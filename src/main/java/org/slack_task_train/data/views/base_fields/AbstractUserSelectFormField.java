package org.slack_task_train.data.views.base_fields;

import com.slack.api.model.block.element.UsersSelectElement;
import com.slack.api.model.view.ViewState;

// https://api.slack.com/reference/block-kit/block-elements#users_select
public abstract class AbstractUserSelectFormField extends AbstractFormField {
    @Override
    public void setState(final ViewState.Value value) {
        if (value.getSelectedUser() != null) {
            setValue(value.getSelectedUser());
        }
    }

    protected UsersSelectElement getUserSelect(final String text) {
        return UsersSelectElement.builder()
                .actionId(getId())
                .placeholder(asText(text, false))
                .build();
    }
}
