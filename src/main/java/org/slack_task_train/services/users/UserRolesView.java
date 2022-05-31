package org.slack_task_train.services.users;

import lombok.Getter;
import org.slack_task_train.data.views.AbstractView;
import org.slack_task_train.data.views.sections.UserSelectSection;
import org.slack_task_train.services.users.accessory.RoleSelectSection;

@Getter
public class UserRolesView extends AbstractView<UserRolesDispatcher> {
    private final UserSelectSection userSelectSection = new UserSelectSection();
    private final RoleSelectSection roleSelectSection = new RoleSelectSection(
            userSelectSection,
            true,
            () -> userSelectSection.getAccessory().isFilled()
    );

    @Override
    public String getName() {
        return "Назначение ролей";
    }
}
