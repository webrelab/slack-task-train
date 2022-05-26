package org.slack_task_train.modules;

import lombok.SneakyThrows;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.services.ifaces.IDispatcher;
import org.slack_task_train.services.runner.AppRunner;

public class SimpleContentDispatcher implements IDispatcher {
    private final SimpleContentView view;

    public SimpleContentDispatcher(SimpleContentView view) {
        this.view = view;
    }

    @SneakyThrows
    @Override
    public void dispatch() {
        SlackTaskTrainApp.slackApp.getApp().getClient().chatPostMessage(m -> m
                .channel(view.getUserId())
                .text(view.getPlainTextSection().getAccessory().getValue())
                .token(AppRunner.SLACK_BOT_TOKEN)
        );
    }
}
