package org.slack_task_train.services.enums;

public enum StartSection {
    REGRESS("Регресс"),
    JENKINS("Jenkins"),
    ADMIN("Администрирование"),
    USER("Пользовательские"),
    RELEASES("Релизы"),
    ;

    private final String desc;

    StartSection(final String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
