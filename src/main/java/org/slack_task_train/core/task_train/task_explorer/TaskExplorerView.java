package org.slack_task_train.core.task_train.task_explorer;

import org.slack_task_train.core.views.AbstractView;
import org.slack_task_train.core.ifaces.ISection;
import org.slack_task_train.core.task_train.task_explorer.components.TaskTrainSection;
import org.slack_task_train.core.task_train.TaskExplorer;
import org.slack_task_train.core.task_train.TaskTrain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskExplorerView extends AbstractView {

    @Override
    public List<ISection> getSections() {
        final Set<TaskTrain> taskTrainList = TaskExplorer.getInstance().getTaskTrainList();
        return taskTrainList
                .stream()
                .map(TaskTrainSection::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "Диспетчер задач";
    }

    @Override
    public void registerViewSubmit() {
        registerViewSubmit(() -> {});
    }
}
