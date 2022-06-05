package org.slack_task_train.core.runner;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.core.ifaces.ICategory;
import org.slack_task_train.core.ifaces.IModuleRegistration;
import org.slack_task_train.core.ifaces.IRoles;

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
    public ICategory getCategory() {
        return null;
    }

    @Override
    public String getButtonId() {
        return null;
    }

    @Override
    public boolean disabled() {
        return false;
    }
}
