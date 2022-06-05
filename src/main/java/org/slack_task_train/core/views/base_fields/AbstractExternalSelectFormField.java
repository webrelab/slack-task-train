package org.slack_task_train.core.views.base_fields;

import com.slack.api.model.block.element.ExternalSelectElement;
import com.slack.api.model.view.ViewState;
import org.slack_task_train.core.ifaces.IHasExternalLoadField;

// Select menu element with external data source
// https://api.slack.com/reference/block-kit/block-elements#external_select
public abstract class AbstractExternalSelectFormField extends AbstractFormField implements IHasExternalLoadField {

    @Override
    public void setState(final ViewState.Value value) {
        if (value.getSelectedOption() != null) {
            setValue(value.getSelectedOption().getValue());
        }
    }

    protected ExternalSelectElement getSelect(
            final String name,
            final int minQueryLength
    ) {
        return ExternalSelectElement.builder()
                                    .actionId(getId())
                                    .placeholder(asText(name, false))
                                    .minQueryLength(minQueryLength)
                                    .build();
    }
}
