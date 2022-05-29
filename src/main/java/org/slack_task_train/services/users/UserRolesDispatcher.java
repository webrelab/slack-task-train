package org.slack_task_train.services.users;

import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.data.models.UserRolesModel;
import org.slack_task_train.services.ifaces.IDispatcher;
import org.slack_task_train.services.users.accessory.UsersHelper;

import java.util.List;
import java.util.Optional;

public class UserRolesDispatcher implements IDispatcher {
    private final UserRolesView view;

    public UserRolesDispatcher(UserRolesView view) {
        this.view = view;
    }

    @Override
    public void dispatch() {
        final String userId = view.getUserSelectSection().getAccessory().getValue();
        final List<String> values = view.getRoleSelectSection().getAccessory().getValues();
        final List<UserRolesModel> savedRoles = SlackTaskTrainApp.slackApp.getUserRolesService().getRolesByUserId(userId);
        final List<String> allRoles = UsersHelper.getRoles();
        allRoles.forEach(r -> {
            if (values.contains(r)) {
                if (savedRoles.stream().noneMatch(m -> m.getUserRole().equals(r))) {
                    final UserRolesModel addRole = new UserRolesModel();
                    addRole.setUserId(userId);
                    addRole.setUserRole(r);
                    SlackTaskTrainApp.slackApp.getUserRolesService().getRepository().save(addRole);
                }
            } else {
                final Optional<UserRolesModel> toRemove = savedRoles.stream().filter(m -> m.getUserRole().equals(r)).findFirst();
                toRemove.ifPresent(m -> SlackTaskTrainApp.slackApp.getUserRolesService().getRepository().delete(m));
            }
        });
    }
}
