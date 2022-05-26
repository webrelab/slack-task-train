package org.slack_task_train.services.views.base_sections;

import org.slack_task_train.services.ifaces.IFormField;
import org.slack_task_train.services.ifaces.IHasExternalLoadField;
import org.slack_task_train.services.ifaces.ISection;
import org.slack_task_train.services.views.Element;
import org.slack_task_train.services.views.base_fields.AbstractFormField;

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
