package org.slack_task_train.example.accessory;

import org.slack_task_train.services.ifaces.IRoles;

public enum ExampleRoles implements IRoles {
    USER(false),
    COPYWRITER(false),
    ADMIN(true)
    ;

    private final boolean isAdmin;

    ExampleRoles(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public String getRoleName() {
        return this.name();
    }

    @Override
    public boolean isAdmin() {
        return isAdmin;
    }
}
