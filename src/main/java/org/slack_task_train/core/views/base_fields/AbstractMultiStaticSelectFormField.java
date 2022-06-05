package org.slack_task_train.core.views.base_fields;

import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.MultiStaticSelectElement;
import com.slack.api.model.view.ViewState;
import org.slack_task_train.core.SlackTaskTrainException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// https://api.slack.com/reference/block-kit/block-elements#multi_select
public abstract class AbstractMultiStaticSelectFormField extends AbstractFormField {
    private final List<String> values = new ArrayList<>();

    @Override
    public boolean isFilled() {
        return !values.isEmpty();
    }

    @Override
    public String getValue() {
        throw new SlackTaskTrainException("Поле не поддерживает получение единичного значения");
    }

    @Override
    public void setState(final ViewState.Value value) {
        values.clear();
        values.addAll(
                value.getSelectedOptions()
                     .stream()
                     .map(ViewState.SelectedOption::getValue)
                     .collect(Collectors.toList())
        );
    }

    @Override
    public void clearState() {
        values.clear();
    }

    @Override
    public List<String> getValues() {
        return values;
    }

    public MultiStaticSelectElement getSelectList(final List<OptionObject> options) {
        return MultiStaticSelectElement.builder()
                .actionId(getId())
                .options(options)
                .build();
    }
}
