package org.slack_task_train.core.users;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.core.ifaces.ICategory;
import org.slack_task_train.core.ifaces.IRoles;
import org.slack_task_train.core.runner.AbstractModuleRegistration;

public class UserRolesRegistration extends AbstractModuleRegistration<UserRolesView> {
    @Override
    public LayoutBlock getStartButton() {
        return createButtonWithDescription("Форма для прямого назначения ролей пользователям");
    }

    @Override
    public IRoles[] acceptedRoles() {
        return new IRoles[0];
    }

    @Override
    public String getName() {
        return "Назначение ролей";
    }

    @Override
    public ICategory getCategory() {
        return () -> "Администрирование";
    }
}
