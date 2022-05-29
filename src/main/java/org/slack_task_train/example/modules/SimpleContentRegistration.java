package org.slack_task_train.example.modules;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.example.accessory.ExampleCategories;
import org.slack_task_train.example.accessory.ExampleRoles;
import org.slack_task_train.services.ifaces.ICategory;
import org.slack_task_train.services.ifaces.IRoles;
import org.slack_task_train.services.runner.AbstractModuleRegistration;

public class SimpleContentRegistration extends AbstractModuleRegistration {
    @Override
    public LayoutBlock getStartButton() {
        return getStartButton("Создать статью", "Создание новой статьи");
    }

    @Override
    public void registerStartButton() {
        registerStartButton(SimpleContentView::new);
    }

    @Override
    public IRoles[] acceptedRoles() {
        return new IRoles[]{ExampleRoles.USER};
    }

    @Override
    public String getName() {
        return "Создать статью";
    }

    @Override
    public ICategory getStartSection() {
        return ExampleCategories.FIRST;
    }
}
