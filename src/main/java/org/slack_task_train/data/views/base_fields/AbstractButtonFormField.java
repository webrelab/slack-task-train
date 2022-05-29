package org.slack_task_train.data.views.base_fields;

import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.view.ViewState;
import org.slack_task_train.services.ifaces.IHasButtonField;

// https://api.slack.com/reference/block-kit/block-elements#button
public abstract class AbstractButtonFormField extends AbstractFormField implements IHasButtonField {
    @Override
    public void setState(final ViewState.Value value) {
        // do nothing
    }

    protected ButtonElement getButton(final String text, final String value) {
        return ButtonElement.builder()
                            .actionId(getId())
                            .text(asText(text, true))
                            .value(value)
                            .build();
    }
}
