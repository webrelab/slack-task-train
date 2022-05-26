package org.slack_task_train.services.views.base_fields;

import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.PlainTextInputElement;
import com.slack.api.model.view.ViewState;

import java.util.Objects;

public class PlainTextField extends AbstractFormField {
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

    @Override
    public BlockElement getElement() {
        return PlainTextInputElement.builder()
                .actionId(getId())
                .multiline(true)
                .maxLength(3000)
                .build();
    }

    @Override
    public String getName() {
        return "Форматированный текст";
    }
}
