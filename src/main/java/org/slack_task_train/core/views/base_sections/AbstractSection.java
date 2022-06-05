package org.slack_task_train.core.views.base_sections;

import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.SectionBlock;
import org.slack_task_train.core.SlackTaskTrainException;
import org.slack_task_train.core.views.Element;
import org.slack_task_train.core.views.base_fields.AbstractPlainTextField;
import org.slack_task_train.core.ifaces.IFormField;
import org.slack_task_train.core.ifaces.IHasExternalLoadField;
import org.slack_task_train.core.ifaces.ISection;
import org.slack_task_train.core.views.base_fields.AbstractFormField;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

public abstract class AbstractSection extends Element implements ISection {
    private final String id = UUID.randomUUID().toString();
    private final AbstractFormField accessory;
    private BooleanSupplier[] constructConditions;
    private Boolean isAndCondition;

    protected AbstractSection(final AbstractFormField accessory) {
        this.accessory = accessory;
        if (accessory instanceof IHasExternalLoadField) {
            ((IHasExternalLoadField) accessory).callback();
        }
    }

    protected AbstractSection(
            final AbstractFormField accessory,
            final boolean isAndCondition,
            final BooleanSupplier... constructConditions
    ) {
        this.accessory = accessory;
        if (accessory instanceof IHasExternalLoadField) {
            ((IHasExternalLoadField) accessory).callback();
        }
        this.isAndCondition = isAndCondition;
        this.constructConditions = constructConditions;
    }

    public SectionBlock getSectionBlock(final String text, final IFormField accessory) {
        if (accessory instanceof AbstractPlainTextField) {
            throw new SlackTaskTrainException("Для текстового поля необходимо использовать метод getInputBlock()");
        }
        return SectionBlock.builder()
                .accessory(accessory.getElement())
                .text(asText(text, true))
                .blockId(getId())
                .build();
    }

    public InputBlock getInputBlock(final String text, final IFormField textField) {
        if (!(accessory instanceof AbstractPlainTextField)) {
            throw new SlackTaskTrainException("Для НЕ текстового поля необходимо использовать метод getSectionBlock()");
        }
        return InputBlock.builder()
                .label(asText(text, true))
                .blockId(getId())
                .element(textField.getElement())
                .build();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean constructCondition() {
        if (isAndCondition == null) {
            return true;
        }
        return isAndCondition ? Stream.of(constructConditions).allMatch(BooleanSupplier::getAsBoolean) :
                Stream.of(constructConditions).anyMatch(BooleanSupplier::getAsBoolean);
    }

    @Override
    public IFormField getAccessory() {
        return accessory;
    }
}
