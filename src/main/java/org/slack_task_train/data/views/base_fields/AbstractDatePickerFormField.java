package org.slack_task_train.data.views.base_fields;

import com.slack.api.model.block.element.DatePickerElement;
import com.slack.api.model.view.ViewState;

import java.util.Objects;

// https://api.slack.com/reference/block-kit/block-elements#datepicker
public abstract class AbstractDatePickerFormField extends AbstractFormField {

    @Override
    public void setState(final ViewState.Value value) {
        if (
                Objects.isNull(value) ||
                Objects.isNull(value.getSelectedDate())
        ) {
            clearState();
        } else {
            setValue(value.getSelectedDate());
        }
    }

    protected DatePickerElement getDatePickerElement() {
        return DatePickerElement.builder().actionId(getId()).build();
    }
}
