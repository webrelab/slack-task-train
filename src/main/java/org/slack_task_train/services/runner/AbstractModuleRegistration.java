package org.slack_task_train.services.runner;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.views.ViewsOpenResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewTitle;
import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.services.ifaces.IDemon;
import org.slack_task_train.services.ifaces.IModuleRegistration;
import org.slack_task_train.services.ifaces.IView;
import org.slack_task_train.data.views.AbstractView;
import org.slack_task_train.data.views.Element;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractModuleRegistration extends Element implements IModuleRegistration {
    public static final App APP = SlackTaskTrainApp.slackApp.getApp();
    public static final String BOT_TOKEN = AppRunner.SLACK_BOT_TOKEN;
    private final String buttonId = UUID.randomUUID().toString();
    private String userId;

    @Override
    public String getButtonId() {
        return buttonId;
    }

    protected LayoutBlock getStartButton(
            final String buttonText,
            final String description
    ) {
        final ButtonElement buttonElement = ButtonElement.builder()
                                                         .actionId(getButtonId())
                                                         .text(asText(buttonText, false))
                                                         .build();
        return SectionBlock.builder()
                           .text(asText(
                                   description,
                                   false
                           ))
                           .accessory(buttonElement)
                           .build();
    }

    protected void registerStartButton(final Supplier<AbstractView> view) {
        log.info("Регистрация базового модуля '{}' ID: {}", getName(), getButtonId());
        APP.blockAction(getButtonId(), (req, ctx) -> {
            userId = req.getPayload().getUser().getId();
            return registerStartButton(ctx, view.get());
        });
    }

    private Response registerStartButton(final ActionContext ctx, final IView IView)
            throws IOException, SlackApiException {
        IView.setUserId(userId);
        IView.registerViewUpdate();
        IView.registerViewSubmit();
        final View view = IView.getView();
        if (view.getTitle().getText().length() > 25) {
            view.setTitle(ViewTitle.builder().type("plain_text").text(view.getTitle().getText().substring(0, 24)).build());
            log.error("Внимание! Название модального окна обрезано до 25 символов");
        }
        final ViewsOpenResponse r = ctx.client()
                                       .viewsOpen(v -> v
                   .triggerId(ctx.getTriggerId())
                   .token(BOT_TOKEN)
                   .view(view)
           );
        if (!r.isOk()) {
            log.error("{}", r);
        }
        return ctx.ack();
    }

    @Override
    public boolean isDemon() {
        return false;
    }

    @Override
    public IDemon getDemon() {
        return null;
    }
}
