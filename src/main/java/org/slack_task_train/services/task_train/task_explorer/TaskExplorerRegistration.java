package org.slack_task_train.services.task_train.task_explorer;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.services.ifaces.ICategory;
import org.slack_task_train.services.ifaces.IRoles;
import org.slack_task_train.services.runner.AbstractModuleRegistration;

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
