package org.slack_task_train.example.modules.simple_content;

import lombok.SneakyThrows;
import org.slack_task_train.App;
import org.slack_task_train.core.ifaces.IDispatcher;
import org.slack_task_train.core.runner.AppRunner;

public class SimpleContentDispatcher implements IDispatcher {
    private final SimpleContentView view;

    public SimpleContentDispatcher(SimpleContentView view) {
        this.view = view;
    }

    @SneakyThrows
    @Override
    public void dispatch() {
        App.slackApp.getApp().getClient().chatPostMessage(m -> m
                .channel(view.getUserId())
                .text(view.getPlainTextSection().getAccessory().getValue())
                .token(AppRunner.SLACK_BOT_TOKEN)
        );
    }
}
