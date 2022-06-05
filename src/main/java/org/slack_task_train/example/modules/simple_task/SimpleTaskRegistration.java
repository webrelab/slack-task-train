package org.slack_task_train.example.modules.simple_task;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.example.accessory.ExampleCategories;
import org.slack_task_train.example.accessory.ExampleRoles;
import org.slack_task_train.core.ifaces.ICategory;
import org.slack_task_train.core.ifaces.IRoles;
import org.slack_task_train.core.runner.AbstractModuleRegistration;

public class SimpleTaskRegistration extends AbstractModuleRegistration<SimpleTaskView> {
    @Override
    public LayoutBlock getStartButton() {
        return createButtonWithDescription("Пример запуска набора связанных задач");
    }

    @Override
    public IRoles[] acceptedRoles() {
        return new IRoles[]{ExampleRoles.USER};
    }

    @Override
    public String getName() {
        return "Example task";
    }

    @Override
    public ICategory getCategory() {
        return ExampleCategories.EXAMPLE_CATEGORY;
    }
}
