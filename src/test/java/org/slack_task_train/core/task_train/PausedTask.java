package org.slack_task_train.core.task_train;

import org.slack_task_train.core.Utils;
import org.slack_task_train.core.enums.TaskExecutionStatus;

public class PausedTask extends AbstractTask {
    private TaskExecutionStatus status = TaskExecutionStatus.NOT_STARTED;

    @Override
    public TaskExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void preExecution() {

    }

    @Override
    public void executeTask() {
        Utils.freeze(3000);
        status = TaskExecutionStatus.SUCCESS;
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
        return "PausedTask";
    }
}
