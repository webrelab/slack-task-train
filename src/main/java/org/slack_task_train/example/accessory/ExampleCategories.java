package org.slack_task_train.example.accessory;

import org.slack_task_train.core.ifaces.ICategory;

public enum ExampleCategories implements ICategory {
    EXAMPLE_CATEGORY("Примеры"),
    ;

    private final String category;

    ExampleCategories(String category) {
        this.category = category;
    }

    @Override
    public String getCategory() {
        return category;
    }
}
