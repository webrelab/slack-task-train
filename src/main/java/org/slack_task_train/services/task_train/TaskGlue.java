package org.slack_task_train.services.task_train;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.Utils;
import org.slack_task_train.services.enums.TaskExecutionStatus;
import org.slack_task_train.services.ifaces.ITask;
import org.slack_task_train.services.runner.SlackMethods;
import org.slack_task_train.services.timer.Timer;

import java.util.ArrayList;
import java.util.Arrays;
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
public class TaskGlue {
    private final ConcurrentLinkedQueue<Dependent> queue = new ConcurrentLinkedQueue<>();
    private boolean stop;
    private final List<BooleanSupplier> queueStartConditions = new ArrayList<>();
    private final List<BooleanSupplier> queueCompleteConditions = new ArrayList<>();
    private final String name;
    private String initiateByUser;
    private boolean isActive;
    private final String uuid = UUID.randomUUID().toString();
    private final Map<Dependent, LongAdder> fatalErrorCounter = new HashMap<>();
    private Dependent currentDependent;
    private final boolean isProtected;

    public TaskGlue(final String name, final String initiateByUser) {
        this.name = name;
        this.initiateByUser = initiateByUser;
        TaskExplorer.getInstance().registerTaskGlue(this);
        isProtected = false;
    }

    public TaskGlue(final String name, final String initiateByUser, final boolean isProtected) {
        this.name = name;
        this.initiateByUser = initiateByUser;
        TaskExplorer.getInstance().registerTaskGlue(this);
        this.isProtected = isProtected;
    }

    public TaskGlue(final String name, final ITask dependentTask, final BooleanSupplier... queueCompleteConditions) {
        final Dependent dependent = new Dependent(dependentTask);
        queue.offer(dependent);
        this.queueCompleteConditions.addAll(Arrays.asList(queueCompleteConditions));
        this.name = name;
        TaskExplorer.getInstance().registerTaskGlue(this);
        isProtected = false;
    }

    public TaskGlue addQueueStartCondition(final BooleanSupplier queueStartCondition) {
        queueStartConditions.add(queueStartCondition);
        return this;
    }

    public TaskGlue addQueueCompleteCondition(final BooleanSupplier queueCompleteCondition) {
        queueCompleteConditions.add(queueCompleteCondition);
        return this;
    }

    public TaskGlue add(final ITask dependentTask) {
        final Dependent dependent = new Dependent(dependentTask);
        queue.offer(dependent);
        return this;
    }

    public TaskGlue add(
            final ITask dependentTask,
            final ITask sourceTask,
            final boolean isAndCondition,
            final BooleanSupplier... conditions
    ) {
        final Source source = new Source(sourceTask, isAndCondition, conditions);
        if (queue.stream().anyMatch(d -> d.getDependent().equals(dependentTask))) {
            queue
                    .stream()
                    .filter(d -> d.getDependent().equals(dependentTask))
                    .findFirst()
                    .ifPresent(d -> d.addSource(source));
        } else {
            final Dependent dependent = new Dependent(dependentTask);
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
            throw new TaskTrainException("Не задано условие для завершения выполнения задания queueCompleteCondition");
        }
        new Thread(this::executor, name).start();
        isActive = true;
    }

    private void executor() {
        final long IDLE_CONVEYOR_MILLIS = 2000L;
        // ждем, пока не наступит условие для старта очереди
        while (!stop && !checkTaskGlueStartCondition()) {
            Utils.freeze(IDLE_CONVEYOR_MILLIS);
        }
        if (Objects.nonNull(initiateByUser) && !initiateByUser.isEmpty() && !stop) {
            SlackMethods.sendMessage("Очередь запущена '" + name + "'", initiateByUser);
        }
        while (!stop && !checkTaskGlueEndCondition()) {
            currentDependent = queue.poll();
            if (Objects.isNull(currentDependent)) {
                Utils.freeze(IDLE_CONVEYOR_MILLIS);
                continue;
            }
            // сразу возвращаем в очередь если задание уже выполнено
            if (currentDependent.getStatus() == TaskExecutionStatus.SUCCESS) {
                queue.offer(currentDependent);
                Utils.freeze(IDLE_CONVEYOR_MILLIS);
                continue;
            }
            try {
                // обновляем текущий статус выполнения задания
                currentDependent.setStatus(currentDependent.getDependent().getStatus());
                switch (currentDependent.getStatus()) {
                    // заново проверяем не выполнено ли оно
                    case SUCCESS:
                        log.info("Выполнение задачи завершено {}", currentDependent.getDependent().getTaskName());
                        currentDependent.getDependent().postExecution();
                        currentDependent.getDependent().saveLastUpdateTime();
                        queue.offer(currentDependent);
                        break;
                    case NOT_STARTED:
                        if (checkCondition(currentDependent)) {
                            currentDependent.getDependent().startTime();
                            currentDependent.getDependent().preExecution();
                            log.info("Задача поставлена на выполнение {}", currentDependent.getDependent().getTaskName());
                            currentDependent.getDependent().executeTask();
                            currentDependent.getDependent().saveLastUpdateTime();
                            currentDependent.getDependent().taskExecutionIncrement();
                        }
                        break;
                    case REPEATABLE:
                        if (currentDependent.getDependent().isIdleTimeOut()) {
                            log.info("Выполнение задачи '{}' по таймеру", currentDependent.getDependent().getTaskName());
                            currentDependent.getDependent().executeTask();
                            currentDependent.getDependent().saveLastUpdateTime();
                            currentDependent.getDependent().taskExecutionIncrement();
                        }
                        break;
                    case IN_PROGRESS:
                        currentDependent.getDependent().saveLastUpdateTime();
                        break;
                    case FAILED:
                        currentDependent.getDependent().saveLastUpdateTime();
                        final String userId = Objects.nonNull(currentDependent.getDependent().getView()) ?
                                currentDependent.getDependent().getView().getUserId() : initiateByUser;
                        final ManageFailedTasksButton manageFailedTasksButton = new ManageFailedTasksButton(this);
                        manageFailedTasksButton.postMessage(currentDependent, userId);
                        queue.offer(currentDependent);
                        final BooleanSupplier waitReaction = () -> currentDependent.getStatus() !=
                                                                    TaskExecutionStatus.FAILED || stop;
                        if (!Timer.executeTimer(6000, waitReaction)) {
                            stop = true;
                        }
                        break;
                    default:
                        throw new TaskTrainException("Не реализована проверка статуса " + currentDependent.getStatus().name());
                }
                queue.offer(currentDependent);
            } catch (final Throwable e) {
                log.error("Ошибка при исполнении задания {}", currentDependent.getDependent().getTaskName());
                log.error("", e);
                // останавливаем очередь на минуту
                Utils.freeze(IDLE_CONVEYOR_MILLIS * 30);
                if (!fatalErrorCounter.containsKey(currentDependent)) {
                    fatalErrorCounter.put(currentDependent, new LongAdder());
                }
                fatalErrorCounter.get(currentDependent).increment();
                // если задание упало с экспшеном больше 10 раз, прерываем исполнение всей нитки
                if (fatalErrorCounter.get(currentDependent).sum() > 10) {
                    if (Objects.nonNull(initiateByUser)) {
                        SlackMethods.sendMessage(
                                String.format("Ошибка при выполнении задания '%s'\n%s\nВыполнение задания прекращено",
                                        currentDependent.getDependent().getTaskName(), e
                                ), initiateByUser);
                    }
                    stop = true;
                }
                queue.offer(currentDependent);
            }
            Utils.freeze(IDLE_CONVEYOR_MILLIS);
        }
        if (stop) {
            log.info("Выполнение сценария '{}' прекращено досрочно", name);
            if (Objects.nonNull(initiateByUser)) {
                SlackMethods.sendMessage(
                        String.format("Выполнение сценария '%s' прекращено досрочно", name),
                        initiateByUser
                );
            }
        }
        if (checkTaskGlueEndCondition()) {
            log.info("Все задачи из сценария '{}' выполнены успешно", name);
            queue.forEach(task -> task.setStatus(TaskExecutionStatus.SUCCESS));
            if (Objects.nonNull(initiateByUser)) {
                SlackMethods.sendMessage(
                        String.format("Все задачи из сценария '%s' выполнены успешно", name),
                        initiateByUser
                );
            }
        }
        isActive = false;
        // очищаем очередь и удаляем ссылку на таску на случай, если поток останется активным
        queue.clear();
        currentDependent = null;
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

    private boolean checkTaskGlueStartCondition() {
        if (queueStartConditions.isEmpty()) {
            return true;
        }
        return queueStartConditions.stream().allMatch(BooleanSupplier::getAsBoolean);
    }

    private boolean checkTaskGlueEndCondition() {
        return queueCompleteConditions.stream().allMatch(BooleanSupplier::getAsBoolean);
    }

    private boolean checkCondition(final Dependent dependent) {
        if (dependent.getSources().isEmpty()) {
            return true;
        }
        return dependent.getSources().stream().allMatch(Source::checkCondition);
    }

    @Getter
    public static class Dependent {
        private final ITask dependent;
        private TaskExecutionStatus status = TaskExecutionStatus.NOT_STARTED;
        private final List<Source> sources = new ArrayList<>();

        Dependent(final ITask dependent) {
            this.dependent = dependent;
        }

        public void addSource(final Source source) {
            sources.add(source);
        }

        public void setStatus(final TaskExecutionStatus status) {
            this.status = status;
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
            log.info("Очищены условия запуска для задачи '{}'", source.getTaskName());
            isAndCondition = null;
        }
    }
}
