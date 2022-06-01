package org.slack_task_train.services.task_train;

import org.slack_task_train.services.enums.TaskExecutionStatus;

class ChangeStatusTask extends AbstractTask {
    private final TaskExecutionStatus statusToChange;
    private TaskExecutionStatus status = TaskExecutionStatus.NOT_STARTED;

    public ChangeStatusTask(TaskExecutionStatus statusToChange) {
        this.statusToChange = statusToChange;
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
        status = statusToChange;
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
        return "Задача со сменой статуса на " + statusToChange;
    }
}
