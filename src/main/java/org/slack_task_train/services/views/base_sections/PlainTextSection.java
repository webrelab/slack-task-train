package org.slack_task_train.services.views.base_sections;

import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.services.views.base_fields.PlainTextField;

import java.util.function.BooleanSupplier;

public class PlainTextSection extends AbstractSection {

    public PlainTextSection() {
        super(new PlainTextField());
    }

    public PlainTextSection(boolean isAndCondition, BooleanSupplier... constructConditions) {
        super(new PlainTextField(), isAndCondition, constructConditions);
    }

    @Override
    public LayoutBlock construct() {
        return InputBlock.builder()
                .element(getAccessory().getElement())
                .blockId(getId())
                .label(asText("Введите текст", false))
                .build();
    }
}
