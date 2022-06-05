package org.slack_task_train.example.accessory;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElement;
import org.slack_task_train.core.views.base_fields.AbstractPlainTextField;
import org.slack_task_train.core.views.base_sections.AbstractSection;

import java.util.function.BooleanSupplier;

public class PlainTextSection extends AbstractSection {

    public PlainTextSection() {
        super(new Accessory());
    }

    public PlainTextSection(boolean isAndCondition, BooleanSupplier... constructConditions) {
        super(new Accessory(), isAndCondition, constructConditions);
    }

    @Override
    public LayoutBlock construct() {
        return getInputBlock("Введите текст", getAccessory());
    }

    private static class Accessory extends AbstractPlainTextField {

        @Override
        public BlockElement getElement() {
            return getTextField(true, 3000);
        }

        @Override
        public String getName() {
            return "Введите текст";
        }
    }
}
