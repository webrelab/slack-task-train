package org.slack_task_train.services.task_train;

import org.slack_task_train.services.enums.TaskExecutionStatus;

class PostExecutionTask extends AbstractTask {
    private TaskExecutionStatus status = TaskExecutionStatus.NOT_STARTED;
    private boolean preExecution;
    private boolean execution;
    private boolean postExecution;


    @Override
    public TaskExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void preExecution() {
    }

    @Override
    public void executeTask() {
        status = TaskExecutionStatus.SUCCESS;
    }

    @Override
    public void postExecution() {
        postExecution = true;
    }

    @Override
    public void reExecuteTask() {

    }

    @Override
    public void throwsException() {

    }

    @Override
    public String getTaskName() {
        return "PostExecutionTask";
    }

    public boolean getPreExecution() {
        return preExecution;
    }

    public boolean getExecution() {
        return execution;
    }

    public boolean getPostExecution() {
        return postExecution;
    }
}
