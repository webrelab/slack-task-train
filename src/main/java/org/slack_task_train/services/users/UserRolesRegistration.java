package org.slack_task_train.services.users;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.services.ifaces.ICategory;
import org.slack_task_train.services.ifaces.IRoles;
import org.slack_task_train.services.runner.AbstractModuleRegistration;

public class UserRolesRegistration extends AbstractModuleRegistration {
    @Override
    public LayoutBlock getStartButton() {
        return getStartButton(getName(), "Форма для прямого назначения ролей пользователям");
    }

    @Override
    public void registerStartButton() {
        registerStartButton(UserRolesView::new);
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
    public ICategory getStartSection() {
        return () -> "Администрирование";
    }
}
