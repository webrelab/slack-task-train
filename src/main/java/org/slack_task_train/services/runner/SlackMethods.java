package org.slack_task_train.services.runner;

import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.ConversationType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.SlackTaskTrainException;
import org.slack_task_train.services.timer.Timer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

@Slf4j
public class SlackMethods {

    public static void sendServiceMessage(final String message) {
        sendMessage(message, SlackTaskTrainApp.slackApp.getConfig().getServiceMessageChannel());
    }
    @SneakyThrows
    public static void sendMessage(final String message, final String channel) {
        final ChatPostMessageResponse response = SlackTaskTrainApp.slackApp.getApp().getClient().chatPostMessage(r -> r
                .text(message)
                .token(AppRunner.SLACK_BOT_TOKEN)
                .channel(channel)
                .mrkdwn(true)
        );
        if (!response.isOk()) {
            log.error("{}", response);
        }
    }

    public static Optional<Conversation> getChannelsByName(final String channelName) {
        return getAllChannels(new ArrayList<>(), "")
                .stream()
                .filter(c -> c.getName().equals(channelName))
                .findFirst();
    }

    public static void channelCreation(final String channelName) {
        final AtomicReference<ConversationsCreateResponse> response = new AtomicReference<>();
        final BooleanSupplier waitWhenRequestBeDone = () -> {
            try {
                response.set(SlackTaskTrainApp.slackApp.getApp().getClient().conversationsCreate(r -> r
                                .isPrivate(false)
                                .token(AppRunner.SLACK_BOT_TOKEN)
                                .name(channelName)
                        )
                );
            } catch (final SlackApiException e) {
                throw new SlackTaskTrainException("Ошибка API Slack", e);
            } catch (final IOException e) {
                log.error("{}", response.get());
                return false;
            }
            return true;
        };
    }

    private static List<Conversation> getAllChannels(final List<Conversation> conversations, final String cursor) {
        final AtomicReference<ConversationsListResponse> response = new AtomicReference<>();
        final BooleanSupplier waitWhenRequestBeDone = () -> {
            try {
                response.set(SlackTaskTrainApp.slackApp.getApp().getClient().conversationsList(r -> r
                        .limit(300)
                        .excludeArchived(false)
                        .types(Collections.singletonList(ConversationType.PUBLIC_CHANNEL))
                        .cursor(cursor)
                        .token(AppRunner.SLACK_BOT_TOKEN)
                ));
            } catch (SlackApiException e) {
                throw new SlackTaskTrainException("Ошибка API Slack", e);
            } catch (IOException e) {
                return false;
            }
            return true;
        };
        if (Timer.executeTimer(30, waitWhenRequestBeDone)) {
            if (!response.get().isOk()) {
                log.error("{}", response.get());
                return conversations;
            }
            conversations.addAll(response.get().getChannels());
            if (Objects.nonNull(response.get().getResponseMetadata().getNextCursor()) && !response.get().getResponseMetadata().getNextCursor().isEmpty()) {
                return getAllChannels(conversations, response.get().getResponseMetadata().getNextCursor());
            }
        } else {
            log.error("Не удалось получить данные о каналах из-за недоступности сервиса");
        }
        return conversations;
    }
}
