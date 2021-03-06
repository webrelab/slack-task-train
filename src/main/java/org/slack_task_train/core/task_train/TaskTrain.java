package org.slack_task_train.core.task_train;

import com.slack.api.methods.SlackApiException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.core.Utils;
import org.slack_task_train.core.enums.TaskExecutionStatus;
import org.slack_task_train.core.ifaces.ITask;
import org.slack_task_train.core.runner.SlackMethods;
import org.slack_task_train.core.timer.Timer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

@Slf4j
public class TaskTrain {
    private final ConcurrentLinkedQueue<Dependent> queue = new ConcurrentLinkedQueue<>();
    private long idleConveyorMillis = 1000L;
    private boolean stop;
    private final List<BooleanSupplier> queueStartConditions = new ArrayList<>();
    private final List<BooleanSupplier> queueCompleteConditions = new ArrayList<>();
    private final String name;
    private final String initiateByUser;
    private boolean isActive;
    private final String uuid = UUID.randomUUID().toString();
    private final Map<Dependent, LongAdder> fatalErrorCounter = new HashMap<>();
    private Dependent currentDependent;
    private final boolean isProtected;

    public TaskTrain(final String name, final String initiateByUser) {
        this.name = Objects.requireNonNull(name);
        this.initiateByUser = Objects.requireNonNull(initiateByUser);
        TaskExplorer.getInstance().registerTaskTrain(this);
        isProtected = false;
    }

    public TaskTrain(final String name, final String initiateByUser, final boolean isProtected) {
        this.name = Objects.requireNonNull(name);
        this.initiateByUser = Objects.requireNonNull(initiateByUser);
        TaskExplorer.getInstance().registerTaskTrain(this);
        this.isProtected = isProtected;
    }

    public TaskTrain addQueueStartCondition(final BooleanSupplier queueStartCondition) {
        queueStartConditions.add(queueStartCondition);
        return this;
    }

    public TaskTrain addQueueCompleteCondition(final BooleanSupplier queueCompleteCondition) {
        queueCompleteConditions.add(queueCompleteCondition);
        return this;
    }

    public TaskTrain add(final ITask dependentTask) {
        final Dependent dependent = new Dependent(Objects.requireNonNull(dependentTask));
        queue.offer(dependent);
        return this;
    }

    public TaskTrain add(
            final ITask dependentTask,
            final ITask sourceTask,
            final boolean isAndCondition,
            final BooleanSupplier... conditions
    ) {
        final Source source = new Source(Objects.requireNonNull(sourceTask), isAndCondition, conditions);
        if (queue.stream().anyMatch(d -> d.getDependent().equals(dependentTask))) {
            queue
                    .stream()
                    .filter(d -> d.getDependent().equals(dependentTask))
                    .findFirst()
                    .ifPresent(d -> d.addSource(source));
        } else {
            final Dependent dependent = new Dependent(Objects.requireNonNull(dependentTask));
            dependent.addSource(source);
            queue.offer(dependent);
        }
        return this;
    }

    public Set<Dependent> getQueue() {
        final Set<Dependent> temporary = new HashSet<>(queue);
        if (Objects.nonNull(currentDependent)) {
            temporary.add(currentDependent);
        }
        return temporary;
    }

    public void stop() {
        this.stop = true;
    }

    public void execute() {
        if (queueCompleteConditions.isEmpty()) {
            throw new TaskTrainException("???? ???????????? ?????????????? ?????? ???????????????????? ???????????????????? ?????????????? queueCompleteCondition");
        }
        new Thread(this::executor, name).start();
        isActive = true;
    }

    private void executor() {
        // ????????, ???????? ???? ???????????????? ?????????????? ?????? ???????????? ??????????????
        while (!stop && !checkTaskTrainStartCondition()) {
            Utils.freeze(idleConveyorMillis);
        }
       SlackMethods.sendMessage("?????????????? ???????????????? '" + name + "'", initiateByUser);
        while (!stop && !checkTaskTrainEndCondition()) {
            currentDependent = queue.poll();
            if (Objects.isNull(currentDependent) || Objects.isNull(currentDependent.getDependent())) {
                Utils.freeze(idleConveyorMillis);
                continue;
            }
            // ?????????? ???????????????????? ?? ?????????????? ???????? ?????????????? ?????? ??????????????????
            if (currentDependent.isPostExecutionDone()) {
                queue.offer(currentDependent);
                Utils.freeze(idleConveyorMillis);
                continue;
            }
            try {
                switch (currentDependent.getDependent().getStatus()) {
                    // ???????????? ?????????????????? ???? ?????????????????? ???? ??????
                    case SUCCESS:
                        successBehavior();
                        break;
                    case NOT_STARTED:
                        notStartedBehavior();
                        break;
                    case REPEATABLE:
                        repeatableBehavior();
                        break;
                    case IN_PROGRESS:
                        currentDependent.getDependent().saveLastUpdateTime();
                        break;
                    case FAILED:
                        failedBehavior();
                        break;
                    default:
                        throw new TaskTrainException("???? ?????????????????????? ???????????????? ?????????????? " + currentDependent.getDependent().getStatus().name());
                }
                queue.offer(currentDependent);
            } catch (final Throwable e) {
                log.error("???????????? ?????? ???????????????????? ?????????????? {}", currentDependent.getDependent().getTaskName());
                log.error("", e);
                // ?????????????????????????? ?????????????? ???? ????????????
                Utils.freeze(idleConveyorMillis * 30);
                if (!fatalErrorCounter.containsKey(currentDependent)) {
                    fatalErrorCounter.put(currentDependent, new LongAdder());
                }
                fatalErrorCounter.get(currentDependent).increment();
                // ???????? ?????????????? ?????????? ?? ?????????????????? ???????????? 10 ??????, ?????????????????? ???????????????????? ???????? ??????????
                if (fatalErrorCounter.get(currentDependent).sum() > 10) {
                        SlackMethods.sendMessage(
                                String.format("???????????? ?????? ???????????????????? ?????????????? '%s'\n%s\n???????????????????? ?????????????? ????????????????????",
                                        currentDependent.getDependent().getTaskName(), e
                                ), initiateByUser);
                    stop = true;
                }
                queue.offer(currentDependent);
            }
            Utils.freeze(idleConveyorMillis);
        }
        if (stop) {
            log.info("???????????????????? ???????????????? '{}' ???????????????????? ????????????????", name);
                SlackMethods.sendMessage(
                        String.format("???????????????????? ???????????????? '%s' ???????????????????? ????????????????", name),
                        initiateByUser
                );
        }
        if (checkTaskTrainEndCondition()) {
            log.info("?????? ???????????? ???? ???????????????? '{}' ?????????????????? ??????????????", name);
                SlackMethods.sendMessage(
                        String.format("?????? ???????????? ???? ???????????????? '%s' ?????????????????? ??????????????", name),
                        initiateByUser
                );
        }
        isActive = false;
        // ?????????????? ?????????????? ?? ?????????????? ???????????? ???? ?????????? ???? ????????????, ???????? ?????????? ?????????????????? ????????????????
        queue.clear();
        currentDependent = null;
    }

    private void successBehavior() {
        log.info("???????????????????? ???????????? ?????????????????? {}", currentDependent.getDependent().getTaskName());
        currentDependent.getDependent().postExecution();
        currentDependent.setPostExecutionDone(true);
        currentDependent.getDependent().saveLastUpdateTime();
    }

    private void notStartedBehavior() {
        if (checkCondition(currentDependent)) {
            currentDependent.getDependent().startTime();
            currentDependent.getDependent().preExecution();
            log.info("???????????? ???????????????????? ???? ???????????????????? {}", currentDependent.getDependent().getTaskName());
            currentDependent.getDependent().executeTask();
            currentDependent.getDependent().saveLastUpdateTime();
            currentDependent.getDependent().taskExecutionIncrement();
        }
    }

    private void repeatableBehavior() {
        if (currentDependent.getDependent().isIdleTimeOut()) {
            log.info("???????????????????? ???????????? '{}' ???? ??????????????", currentDependent.getDependent().getTaskName());
            currentDependent.getDependent().executeTask();
            currentDependent.getDependent().saveLastUpdateTime();
            currentDependent.getDependent().taskExecutionIncrement();
        }
    }

    private void failedBehavior() throws SlackApiException, IOException {
        currentDependent.getDependent().saveLastUpdateTime();
        final ManageFailedTasksButton manageFailedTasksButton = new ManageFailedTasksButton(this);
        manageFailedTasksButton.postMessage(currentDependent, initiateByUser);
        final BooleanSupplier waitReaction = () -> currentDependent.getDependent().getStatus() !=
                TaskExecutionStatus.FAILED || stop;
        if (!Timer.executeTimer(6000, waitReaction)) {
            stop = true;
        }
    }

    public TaskTrain setIdleConveyorMillis(final long idleConveyorMillis) {
        this.idleConveyorMillis = idleConveyorMillis;
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isComplete() {
        return !isActive || stop;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public String getUuid() {
        return uuid;
    }

    private boolean checkTaskTrainStartCondition() {
        if (queueStartConditions.isEmpty()) {
            return true;
        }
        return queueStartConditions.stream().allMatch(BooleanSupplier::getAsBoolean);
    }

    private boolean checkTaskTrainEndCondition() {
        return queueCompleteConditions.stream().allMatch(BooleanSupplier::getAsBoolean)
                && queue.stream().allMatch(Dependent::isPostExecutionDone);
    }

    private boolean checkCondition(final Dependent dependent) {
        if (dependent.getSources().isEmpty()) {
            return true;
        }
        return dependent.getSources().stream().allMatch(Source::checkCondition);
    }

    /**
     * ???????????? ?????? ???????????????? ????????????, ?????????????? ?? ?????????????? ???????????? ???????????? ???????? ???????????????? ?????? ?????????????? ??????????????????????
     * ???????????????????? ?????????????? ?????? ?????????????? ???? ???????? ???????????????? ???????????? sources ???????? ?????????????????? ?? ?????????????? ????????????????????
     */
    @Getter
    public static class Dependent {
        // ???????? ?????? ???????????????? ?????????????? ????????????
        private final ITask dependent;
        private final List<Source> sources = new ArrayList<>();

        private boolean isPostExecutionDone;

        Dependent(final ITask dependent) {
            this.dependent = dependent;
        }

        public void addSource(final Source source) {
            sources.add(source);
        }

        public void setPostExecutionDone(boolean postExecutionDone) {
            isPostExecutionDone = postExecutionDone;
        }

        public boolean isPostExecutionDone() {
            return isPostExecutionDone;
        }
    }

    @Getter
    @Slf4j
    public static class Source {
        private final ITask source;
        private Boolean isAndCondition;
        private final BooleanSupplier[] conditions;

        Source(final ITask source, final Boolean isAndCondition, final BooleanSupplier[] conditions) {
            this.source = source;
            this.isAndCondition = isAndCondition;
            this.conditions = conditions;
        }

        boolean checkCondition() {
            if (isAndCondition == null || conditions.length == 0) {
                return true;
            }
            if (isAndCondition) {
                return Stream.of(conditions).allMatch(BooleanSupplier::getAsBoolean);
            }
            return Stream.of(conditions).anyMatch(BooleanSupplier::getAsBoolean);
        }

        public void clearCondition() {
            log.info("?????????????? ?????????????? ?????????????? ?????? ???????????? '{}'", source.getTaskName());
            isAndCondition = null;
        }
    }
}
