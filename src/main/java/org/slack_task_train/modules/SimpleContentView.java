package org.slack_task_train.modules;

import lombok.Getter;
import org.slack_task_train.services.views.AbstractView;
import org.slack_task_train.services.views.base_sections.PlainTextSection;

@Getter
public class SimpleContentView extends AbstractView {
    private final PlainTextSection plainTextSection = new PlainTextSection();

    @Override
    public String getName() {
        return "Создание простого материала";
    }

    @Override
    public void registerViewSubmit() {
        registerViewSubmit(new SimpleContentDispatcher(this));
    }
}
