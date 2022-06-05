package org.slack_task_train.example.modules.simple_task;

import org.slack_task_train.example.modules.simple_task.accessories.SimpleWaitFirstTask;
import org.slack_task_train.core.ifaces.IDispatcher;
import org.slack_task_train.core.ifaces.ITask;
import org.slack_task_train.core.task_train.TaskTrain;

public class SimpleTaskDispatcher implements IDispatcher {
    private final SimpleTaskView view;

    public SimpleTaskDispatcher(SimpleTaskView view) {
        this.view = view;
    }

    @Override
    public void dispatch() {
        final TaskTrain taskTrain = new TaskTrain("Пример выполнения заданий", view.getUserId());
        ITask task = new SimpleWaitFirstTask(view.getPauseInSecondsSection());
        ITask previousTask;
        taskTrain.addQueueStartCondition(() -> true)
                .add(task);
        for (int i = 0; i < Integer.parseInt(view.getNumberOfTasks().getAccessory().getValue()); i++) {
            previousTask = task;
            task = new SimpleWaitFirstTask(view.getPauseInSecondsSection());
            taskTrain.add(task, previousTask, true, previousTask::isTaskComplete);
        }
        taskTrain.addQueueCompleteCondition(task::isTaskComplete)
                .execute();
    }
}
