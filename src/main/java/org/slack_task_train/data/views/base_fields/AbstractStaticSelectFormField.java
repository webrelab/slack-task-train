package org.slack_task_train.data.views.base_fields;

import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.StaticSelectElement;
import com.slack.api.model.view.ViewState;

import java.util.List;
import java.util.Objects;

// https://api.slack.com/reference/messaging/block-elements#static-select
public abstract class AbstractStaticSelectFormField extends AbstractFormField {

    @Override
    public void setState(final ViewState.Value value) {
        if (
                Objects.isNull(value) ||
                Objects.isNull(value.getSelectedOption()) ||
                Objects.isNull(value.getSelectedOption().getValue()) ||
                "none".equals(value.getSelectedOption().getValue())
        ) {
            clearState();
        } else {
            setValue(value.getSelectedOption().getValue());
        }
    }

    protected StaticSelectElement getSelect(
            final String name,
            final List<OptionObject> options
    ) {
        return StaticSelectElement.builder()
                                  .actionId(getId())
                                  .placeholder(asText(name, false))
                                  .options(options)
                                  .build();
    }
}
