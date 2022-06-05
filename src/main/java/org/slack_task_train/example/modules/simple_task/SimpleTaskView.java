package org.slack_task_train.example.modules.simple_task;

import lombok.Getter;
import org.slack_task_train.core.views.AbstractView;
import org.slack_task_train.example.modules.simple_task.accessories.SimpleTaskNumericSelectSection;

@Getter
public class SimpleTaskView extends AbstractView<SimpleTaskDispatcher> {
    private final SimpleTaskNumericSelectSection pauseInSecondsSection = new SimpleTaskNumericSelectSection(0, 10, 10, "Задержка в секундах");
    private final SimpleTaskNumericSelectSection numberOfTasks = new SimpleTaskNumericSelectSection(1, 1, 10, "Количество заданий");

    @Override
    public String getName() {
        return "Example task";
    }
}
