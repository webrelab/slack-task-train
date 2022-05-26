package org.slack_task_train.services.runner;

import com.slack.api.methods.response.views.ViewsPublishResponse;
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
import org.slack_task_train.services.enums.SlackRoles;
import org.slack_task_train.services.enums.StartSection;
import org.slack_task_train.services.ifaces.IModuleRegistration;
import org.slack_task_train.services.users.SlackUsers;
import org.slack_task_train.services.users.UserRolesRepository;

import java.util.*;

@Slf4j
public
class ServiceRegistration {
    private final Map<IModuleRegistration, SlackRoles[]> buttons = new HashMap<>();

    private ServiceRegistration() {}

    public static void init() {
        final ServiceRegistration serviceRegistration = new ServiceRegistration();
        serviceRegistration.register();
        serviceRegistration.registerInitCommand();
        serviceRegistration.registerEmptyMessageChangeEvent();
    }

    public void register() {
        final Set<Class<? extends IModuleRegistration>> registrationClassSet =
                new Reflections(new ConfigurationBuilder().forPackages("org.slack_task_train")).getSubTypesOf(
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
                for (final SlackRoles role : instance.acceptedRoles()) {
                    UserRolesRepository.getInstance().acceptModule(role, instance.getName());
                }
                instance.registerStartButton();
            }

        });
    }

    public void registerInitCommand() {
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
        final EnumMap<StartSection, List<IModuleRegistration>> splitBySections = new EnumMap<>(StartSection.class);

        buttons.entrySet()
               .stream()
               .filter(e -> UserRolesRepository.getInstance().hasAny(user, e.getValue()))
               .map(Map.Entry::getKey)
               .forEach(sr -> {
                   if (!splitBySections.containsKey(sr.getStartSection())) {
                       splitBySections.put(sr.getStartSection(), new ArrayList<>());
                   }
                   splitBySections.get(sr.getStartSection()).add(sr);
               });

        final List<LayoutBlock> blocks = new ArrayList<>();

        splitBySections.forEach((k, v) -> {
            v.sort(Comparator.comparing(IModuleRegistration::getName));
            blocks.add(
                    SectionBlock.builder()
                                .text(MarkdownTextObject.builder().text("*" + k.getDesc() + "*").build())
                                .build()
            );
            v.forEach(sr -> blocks.add(sr.getStartButton()));
        });

        return blocks;
    }

    public void registerEmptyMessageChangeEvent() {
        SlackTaskTrainApp.slackApp.getApp().event(MessageChangedEvent.class, (req, ctx) -> ctx.ack());
    }
}
