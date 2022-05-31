package org.slack_task_train.example.modules.simple_task.accessories;

import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.Utils;
import org.slack_task_train.services.enums.TaskExecutionStatus;
import org.slack_task_train.services.task_train.AbstractTask;

@Slf4j
public class SimpleWaitFirstTask extends AbstractTask {
    private final SimpleTaskNumericSelectSection pause;

    public SimpleWaitFirstTask(final SimpleTaskNumericSelectSection pause) {
        this.pause = pause;
    }

    private TaskExecutionStatus status = TaskExecutionStatus.NOT_STARTED;

    @Override
    public TaskExecutionStatus getStatus() {
        return status;
    }

    @Override
    public void preExecution() {
        log.info("Выполняется код перед задачей");
    }

    @Override
    public void executeTask() {
        status = TaskExecutionStatus.IN_PROGRESS;
        log.info("Выполняется код задачи");
        Utils.freeze(Integer.parseInt(pause.getAccessory().getValue()) * 1000L);
        log.info("Выполнение кода задачи завершено");
        status = TaskExecutionStatus.SUCCESS;
    }

    @Override
    public void postExecution() {
        log.info("Выполняется код после завершения задачи");
    }

    @Override
    public void reExecuteTask() {

    }

    @Override
    public void throwsException() {

    }

    @Override
    public String getTaskName() {
        return "Задача с ожиданием";
    }
}
