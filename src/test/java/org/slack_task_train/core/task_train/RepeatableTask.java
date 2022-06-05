package org.slack_task_train.core.task_train;

import org.slack_task_train.core.enums.TaskExecutionStatus;

import java.util.function.Supplier;

public class RepeatableTask extends AbstractTask {
    private TaskExecutionStatus status = TaskExecutionStatus.NOT_STARTED;

    @Override
    public Supplier<Long> getIdleDuration() {
        return () -> 100L;
    }

    @Override
    public TaskExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void preExecution() {

    }

    @Override
    public void executeTask() {
        status = TaskExecutionStatus.REPEATABLE;
    }

    @Override
    public void postExecution() {

    }

    @Override
    public void reExecuteTask() {

    }

    @Override
    public void throwsException() {

    }

    @Override
    public String getTaskName() {
        return "RepeatableTask";
    }
}
