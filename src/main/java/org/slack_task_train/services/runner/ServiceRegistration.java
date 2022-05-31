package org.slack_task_train.services.runner;

import com.slack.api.methods.response.views.ViewsPublishResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.User;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.event.AppHomeOpenedEvent;
import com.slack.api.model.event.MessageChangedEvent;
import com.slack.api.model.view.View;
import com.slack.api.model.view.Views;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slack_task_train.SlackTaskTrainException;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.data.models.UserRolesModel;
import org.slack_task_train.data.services.UserRolesService;
import org.slack_task_train.services.ifaces.ICategory;
import org.slack_task_train.services.ifaces.IModuleRegistration;
import org.slack_task_train.services.ifaces.IRoles;
import org.slack_task_train.services.users.SlackUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Component
public class ServiceRegistration {
    @Autowired
    private Configurations config;
    @Autowired
    private UserRolesService userRolesService;
    private final Map<IModuleRegistration, IRoles[]> buttons = new HashMap<>();

    public void init() {
        register();
        registerInitCommand();
        registerEmptyMessageChangeEvent();
        checkServiceChannel();
    }

    private void register() {
        final Set<Class<? extends IModuleRegistration>> registrationClassSet =
                new Reflections(new ConfigurationBuilder().forPackages(config.getModulePackages().split(","))).getSubTypesOf(
                        IModuleRegistration.class);
        registrationClassSet.forEach(clazz -> {
            if (
                    clazz.isAssignableFrom(AbstractModuleRegistration.class)
                    || clazz.isAssignableFrom(AbstractModuleDemonRegistration.class)
            ) {
                return;
            }
            final IModuleRegistration instance;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (final Throwable e) {
                throw new SlackTaskTrainException(e);
            }
            if (instance.isDemon()) {
                new Thread(instance.getDemon()::init, instance.getName()).start();
            } else {
                buttons.put(instance, instance.acceptedRoles());
                instance.registerStartButton();
            }

        });
    }

    private void registerInitCommand() {
        SlackTaskTrainApp.slackApp.getApp().event(AppHomeOpenedEvent.class, (req, ctx) -> {
              final View appHomeView = Views.view(v -> v
                    .type("home")
                    .blocks(getAcceptedBlocks(req.getEvent().getUser()))
            );
            final ViewsPublishResponse res = ctx.client().viewsPublish(r -> r
                    .userId(req.getEvent().getUser())
                    .token(AppRunner.SLACK_BOT_TOKEN)
                    .view(appHomeView)
            );
            if (!res.isOk()) {
                log.error("{}", res);
            }
            return ctx.ack();
        });
    }

    private List<LayoutBlock> getAcceptedBlocks(final String userId) {
        final User user = SlackUsers.getInstance().getUser(userId);
        final Map<ICategory, List<IModuleRegistration>> splitBySections = new HashMap<>();

        buttons.entrySet()
               .stream()
               .filter(e -> isAdmin(userId) || matchRoles(userRolesService.getRolesByUserId(userId), e.getValue()))
               .map(Map.Entry::getKey)
               .forEach(sr -> {
                   if (!splitBySections.containsKey(sr.getCategory())) {
                       splitBySections.put(sr.getCategory(), new ArrayList<>());
                   }
                   splitBySections.get(sr.getCategory()).add(sr);
               });

        final List<LayoutBlock> blocks = new ArrayList<>();

        splitBySections.forEach((k, v) -> {
            v.sort(Comparator.comparing(IModuleRegistration::getName));
            blocks.add(
                    SectionBlock.builder()
                                .text(MarkdownTextObject.builder().text("*" + k.getCategory() + "*").build())
                                .build()
            );
            v.forEach(sr -> blocks.add(sr.getStartButton()));
        });

        return blocks;
    }

    private boolean matchRoles(final List<UserRolesModel> userRolesModels, final IRoles[] expectedRoles) {
        return userRolesModels.stream()
                .map(UserRolesModel::getUserRole)
                .anyMatch(r -> Stream.of(expectedRoles).map(IRoles::getRoleName).anyMatch(e -> e.equals(r)));
    }

    public boolean isAdmin(final String userId) {
        final String[] adminUserIds = System.getenv("adminUserId").split(",");
        if (adminUserIds.length == 0) {
            return false;
        }
        Arrays.sort(adminUserIds);
        return Arrays.binarySearch(adminUserIds, userId) > -1;
    }

    private void registerEmptyMessageChangeEvent() {
        SlackTaskTrainApp.slackApp.getApp().event(MessageChangedEvent.class, (req, ctx) -> ctx.ack());
    }

    // Проверка наличия сервисного канала для публикации сообщений бота и его создание при отсутствии
    private void checkServiceChannel() {
        final Optional<Conversation> conversation = SlackMethods.getChannelsByName(SlackTaskTrainApp.slackApp.getConfig().getServiceMessageChannel());
        if (conversation.isEmpty()) {
            SlackMethods.channelCreation(SlackTaskTrainApp.slackApp.getConfig().getServiceMessageChannel());
        }
    }
}
