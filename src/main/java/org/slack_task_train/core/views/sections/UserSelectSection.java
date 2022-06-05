package org.slack_task_train.core.views.sections;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElement;
import org.slack_task_train.core.views.base_fields.AbstractUserSelectFormField;
import org.slack_task_train.core.views.base_sections.AbstractSection;

import java.util.function.BooleanSupplier;

public class UserSelectSection extends AbstractSection {

    public UserSelectSection() {
        super(new Accessory());
    }

    public UserSelectSection(boolean isAndCondition, BooleanSupplier... constructConditions) {
        super(new Accessory(), isAndCondition, constructConditions);
    }

    @Override
    public LayoutBlock construct() {
        return getSectionBlock("Выбрать пользователя", getAccessory());
    }

    private static class Accessory extends AbstractUserSelectFormField {

        @Override
        public BlockElement getElement() {
            return getUserSelect(getName());
        }

        @Override
        public String getName() {
            return "Выбрать пользователя";
        }
    }
}
