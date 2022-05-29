package org.slack_task_train.services.task_train;

import java.util.HashSet;
import java.util.Set;

// Диспетчер задач. Хранит все созданные нитки TaskGlue и предоставляет доступ к удалению заданий из Main.QUEUE_EXECUTOR
public final class TaskExplorer {
    private static final TaskExplorer INSTANCE = new TaskExplorer();
    private final Set<TaskGlue> taskGlueList = new HashSet<>();

    private TaskExplorer() {
    }

    public static TaskExplorer getInstance() {
        return INSTANCE;
    }

    public void registerTaskGlue(final TaskGlue taskGlue) {
        taskGlueList.add(taskGlue);
    }

    public Set<TaskGlue> getTaskGlueList() {
        taskGlueList.removeIf(TaskGlue::isComplete);
        taskGlueList.removeIf(TaskGlue::isProtected);
        return taskGlueList;
    }
}
