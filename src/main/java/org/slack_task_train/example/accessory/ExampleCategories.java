package org.slack_task_train.example.accessory;

import org.slack_task_train.services.ifaces.ICategory;

public enum ExampleCategories implements ICategory {
    FIRST,
    SECOND
    ;

    @Override
    public String getCategory() {
        return this.name();
    }
}
