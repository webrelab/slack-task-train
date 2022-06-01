package org.slack_task_train.services.task_train;

import org.slack_task_train.Utils;
import org.slack_task_train.services.enums.TaskExecutionStatus;

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
