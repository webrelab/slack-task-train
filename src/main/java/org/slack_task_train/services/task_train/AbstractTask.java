package org.slack_task_train.services.task_train;

import org.slack_task_train.SlackTaskTrainException;
import org.slack_task_train.services.enums.TaskExecutionStatus;
import org.slack_task_train.services.ifaces.ITask;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class AbstractTask implements ITask {
    private int attempts = 3;
    private int executionCounter;
    private LocalDateTime lastUpdateTime;
    private final String uuid = UUID.randomUUID().toString();
    private LocalDateTime executionStartTime;

    @Override
    public Duration getTaskExecutionDuration() {
        final LocalDateTime lastTime;
        final TaskExecutionStatus status = getStatus();
        switch (status) {
            case SUCCESS:
            case FAILED:
                lastTime = lastUpdateTime;
                break;
            default:
                lastTime = LocalDateTime.now();
                break;
        }
        return Objects.isNull(executionStartTime) ? Duration.ZERO : Duration.between(executionStartTime, lastTime);
    }

    @Override
    public void startTime() {
        executionStartTime = LocalDateTime.now();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void saveLastUpdateTime() {
        lastUpdateTime = LocalDateTime.now();
    }

    @Override
    public Supplier<Long> getIdleDuration() {
        throw new TaskTrainException("Метод 'getIdleDuration' не поддерживается, требуется его переопределить");
    }

    @Override
    public boolean isIdleTimeOut() {
        return lastUpdateTime.plus(getIdleDuration().get(), ChronoUnit.MILLIS).isBefore(LocalDateTime.now());
    }

    @Override
    public int taskExecutionCounter() {
        return executionCounter;
    }

    @Override
    public void taskExecutionIncrement() {
        executionCounter++;
    }

    @Override
    public boolean isTaskComplete() {
        return getStatus() == TaskExecutionStatus.SUCCESS;
    }

    @Override
    public void decrementAttempts() {
        attempts--;
    }

    @Override
    public boolean hasAttempts() {
        return attempts > 0;
    }

    protected long getMillisFromMinutes(final int minutes) {
        return minutes * 60 * 1000L;
    }

    @Override
    public void setStatus(final TaskExecutionStatus status) {
        throw new TaskTrainException("Устновка статуса не поддерживается задачей '" + getTaskName() + "'");
    }
}
