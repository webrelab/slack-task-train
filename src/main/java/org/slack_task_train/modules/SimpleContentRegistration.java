package org.slack_task_train.modules;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.services.enums.SlackRoles;
import org.slack_task_train.services.enums.StartSection;
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
    public SlackRoles[] acceptedRoles() {
        return new SlackRoles[]{SlackRoles.USER};
    }

    @Override
    public String getName() {
        return "Создать статью";
    }

    @Override
    public StartSection getStartSection() {
        return StartSection.USER;
    }
}
