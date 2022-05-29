package org.slack_task_train.services.runner;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.services.ifaces.ICategory;
import org.slack_task_train.services.ifaces.IModuleRegistration;
import org.slack_task_train.services.ifaces.IRoles;

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
    public IRoles[] acceptedRoles() {
        return new IRoles[]{};
    }

    @Override
    public ICategory getStartSection() {
        return null;
    }

    @Override
    public String getButtonId() {
        return null;
    }
}
