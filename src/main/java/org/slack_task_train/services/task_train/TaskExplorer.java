package org.slack_task_train.services.task_train;

import java.util.HashSet;
import java.util.Set;

// Диспетчер задач. Хранит все созданные нитки TaskGlue и предоставляет доступ к удалению заданий из Main.QUEUE_EXECUTOR
public final class TaskExplorer {
    private static final TaskExplorer INSTANCE = new TaskExplorer();
    private final Set<TaskTrain> taskTrainList = new HashSet<>();

    private TaskExplorer() {
    }

    public static TaskExplorer getInstance() {
        return INSTANCE;
    }

    public void registerTaskTrain(final TaskTrain taskTrain) {
        taskTrainList.add(taskTrain);
    }

    public Set<TaskTrain> getTaskTrainList() {
        taskTrainList.removeIf(TaskTrain::isComplete);
        taskTrainList.removeIf(TaskTrain::isProtected);
        return taskTrainList;
    }
}
