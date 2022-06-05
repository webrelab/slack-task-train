package org.slack_task_train.core.task_train;

import org.slack_task_train.core.SlackTaskTrainException;
import org.slack_task_train.core.enums.TaskExecutionStatus;

public class ThrowedTask extends AbstractTask {
    TaskExecutionStatus status = TaskExecutionStatus.NOT_STARTED;

    @Override
    public TaskExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void preExecution() {

    }

    @Override
    public void executeTask() {
        throw new SlackTaskTrainException("Исключение для тестирования");
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
        return "Задача с ошибкой";
    }
}
