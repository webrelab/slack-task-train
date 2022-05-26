package org.slack_task_train.services.runner;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.services.enums.SlackRoles;
import org.slack_task_train.services.enums.StartSection;
import org.slack_task_train.services.ifaces.IModuleRegistration;

public abstract class AbstractModuleDemonRegistration implements IModuleRegistration {
    @Override
    public LayoutBlock getStartButton() {
        return null;
    }

    @Override
    public void registerStartButton() {
        // do nothing
    }

    @Override
    public SlackRoles[] acceptedRoles() {
        return new SlackRoles[0];
    }

    @Override
    public StartSection getStartSection() {
        return null;
    }

    @Override
    public String getButtonId() {
        return null;
    }
}
