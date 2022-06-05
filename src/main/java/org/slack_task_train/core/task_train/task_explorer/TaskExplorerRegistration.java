package org.slack_task_train.core.task_train.task_explorer;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.core.ifaces.ICategory;
import org.slack_task_train.core.ifaces.IRoles;
import org.slack_task_train.core.runner.AbstractModuleRegistration;

public class TaskExplorerRegistration extends AbstractModuleRegistration<TaskExplorerView> {

    @Override
    public IRoles[] acceptedRoles() {
        return new IRoles[0];
    }

    @Override
    public LayoutBlock getStartButton() {
        return createButtonWithDescription("Поиск и остановка задач");
    }

    @Override
    public String getName() {
        return "Диспетчер задач";
    }

    @Override
    public ICategory getCategory() {
        return () -> "Администрирование";
    }
}
