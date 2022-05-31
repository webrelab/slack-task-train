package org.slack_task_train.example.modules.simple_task.accessories;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.element.BlockElement;
import org.slack_task_train.data.views.base_fields.AbstractStaticSelectFormField;
import org.slack_task_train.data.views.base_sections.AbstractSection;

import java.util.ArrayList;
import java.util.List;

public class SimpleTaskNumericSelectSection extends AbstractSection {
    private final String name;
    public SimpleTaskNumericSelectSection(final int min, final int duration, final int quantity, final String name) {
        super(new Accessory(min, duration, quantity));
        this.name = name;
    }

    @Override
    public LayoutBlock construct() {
        return getSectionBlock(name, getAccessory());
    }

    private static class Accessory extends AbstractStaticSelectFormField {
        private final int min;
        private final int duration;
        private final int quantity;

        public Accessory(final int min, final int duration, final int quantity) {
            this.min = min;
            this.duration = duration;
            this.quantity = quantity;
        }


        @Override
        public BlockElement getElement() {
            return getSelect("Выбери значение из списка", getOptions());
        }

        private List<OptionObject> getOptions() {
            final List<OptionObject> options = new ArrayList<>();
            for (int i = 0; i < quantity; i++) {
                final String value = String.valueOf(min + duration * i);
                options.add(asOptionObject(value, value));
            }
            return options;
        }

        @Override
        public String getName() {
            return "Выбор из списка";
        }
    }

}
