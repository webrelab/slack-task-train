package org.slack_task_train.core.views.base_fields;

import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.view.ViewState;

import java.util.Objects;

public abstract class AbstractPlainTextField extends AbstractFormField {
    @Override
    public void setState(ViewState.Value value) {
        if (Objects.nonNull(value)) {
            if (Objects.isNull(value.getValue()) || value.getValue().trim().isEmpty()) {
                setValue("");
            } else {
                setValue(value.getValue());
            }
        }
    }

    public BlockElement getTextField(final boolean isMultiline, final int maxLength) {
        return PlainTextInputElement.builder()
                .actionId(getId())
                .multiline(isMultiline)
                .maxLength(maxLength)
                .build();
    }
}
