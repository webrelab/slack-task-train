package org.slack_task_train.services.users.accessory;

import com.slack.api.model.User;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.CheckboxesElement;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.data.models.UserRolesModel;
import org.slack_task_train.data.views.base_fields.AbstractCheckBoxFormField;
import org.slack_task_train.data.views.base_sections.AbstractSection;
import org.slack_task_train.data.views.sections.UserSelectSection;
import org.slack_task_train.services.users.SlackUsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

public class RoleSelectSection extends AbstractSection {

    public RoleSelectSection(final UserSelectSection userSelectSection, boolean isAndCondition, BooleanSupplier... constructConditions) {
        super(new Accessory(userSelectSection), isAndCondition, constructConditions);
    }

    @Override
    public LayoutBlock construct() {
        return getSectionBlock("Выбери роли", getAccessory());
    }

    private static class Accessory extends AbstractCheckBoxFormField {
        private final UserSelectSection userSelectSection;

        public Accessory(final UserSelectSection userSelectSection) {
            this.userSelectSection = userSelectSection;
        }

        @Override
        public BlockElement getElement() {
            final User user = SlackUsers.getInstance().getUser(userSelectSection.getAccessory().getValue());
            if (Objects.isNull(user)) {
                return getCheckBoxList(new ArrayList<>());
            }
            final List<UserRolesModel> roles = SlackTaskTrainApp.slackApp.getUserRolesService().getRolesByUserId(user.getId());
            final CheckboxesElement element = getCheckBoxList(
                    UsersHelper.getRoles().stream()
                            .map(r -> asOptionObject(r, r))
                            .collect(Collectors.toList()));
            final List<OptionObject> initialOptions = roles.stream().map(UserRolesModel::getUserRole).map(r -> asOptionObject(r, r)).collect(Collectors.toList());
            if (!initialOptions.isEmpty()) {
                element.setInitialOptions(initialOptions);
            }
            return element;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
