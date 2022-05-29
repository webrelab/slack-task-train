package org.slack_task_train.data.views;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.views.ViewsUpdateResponse;
import com.slack.api.model.User;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import com.slack.api.model.view.Views;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.SlackTaskTrainException;
import org.slack_task_train.services.ifaces.*;
import org.slack_task_train.services.users.SlackUsers;
import org.slack_task_train.data.views.base_fields.AbstractFormField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class AbstractView extends Element implements IView {
    private final String id = UUID.randomUUID().toString();
    private String userId;

    @Override
    public View getView() {
        return getView(getName());
    }

    @Override
    public synchronized void updateViewState(final Map<String, Map<String, ViewState.Value>> viewState) {
        getSections().forEach(s -> {
            if (viewState.containsKey(s.getId())) {
                if (viewState.get(s.getId()).containsKey(s.getAccessory().getId())) {
                    if (viewState.get(s.getId()).get(s.getAccessory().getId()) != null) {
                        s.getAccessory().setState(viewState.get(s.getId()).get(s.getAccessory().getId()));
                    }
                }
            }
        });
    }

    @SneakyThrows
    @Override
    public List<ISection> getSections() {
        final List<ISection> sections = new ArrayList<>();
        for (final Field field : this.getClass().getDeclaredFields()) {
            if (ISection.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                sections.add((ISection) field.get(this));
                field.setAccessible(false);
            }
        }
        return sections;
    }

    protected List<LayoutBlock> getBlocks() {
        return getSections()
                .stream()
                .filter(ISection::constructCondition)
                .map(ISection::construct)
                .peek(Objects::requireNonNull)
                .flatMap(s -> Stream.of(s, new DividerBlock()))
                .collect(Collectors.toList());
    }

    protected View getView(
            final String title
    ) {
        return Views.view(v -> v
                .type("modal")
                .title(asViewTitle(title, true))
                .callbackId(getId())
                .submit(asViewSubmit("Запуск", true))
                .close(asViewClose("Отмена", true))
                .blocks(getBlocks())
        );
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public IFormField getFieldById(final String id) {
        return getSections().stream()
                            .map(ISection::getAccessory)
                            .filter(a -> id.equals(a.getId()))
                            .findFirst()
                            .orElseThrow(() -> new SlackTaskTrainException("Нет поля с ID " + id));
    }

    @Override
    public void registerViewUpdate() {
        getSections().forEach(this::registerViewUpdate);
    }

    public void registerViewSubmit(final IDispatcher dispatcher) {
        APP.viewSubmission(getId(), (req, ctx) -> {
            updateViewState(req.getPayload().getView().getState().getValues());
            new Thread(() -> {
                final User user = SlackUsers.getInstance().getUser(userId);
                log.info("Пользователь '{}' запустил модуль '{}'",
                        Objects.nonNull(user) ? user.getProfile().getDisplayName() : "Пользователь не определён",
                        getName()
                );
                dispatcher.dispatch();
            }, "User start module notification").start();
            return ctx.ack();
        });
    }

    public void registerViewUpdate(final ISection section) {
        if (section.getAccessory() == null) {
            return;
        }
        APP.blockAction(section.getAccessory().getId(), (req, ctx) -> {
            updateView(req, ctx, section);
            return ctx.ack();
        });
    }

    @Override
    public final String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(final String userId) {
        this.userId = userId;
    }

    private synchronized void updateView(final BlockActionRequest req, final ActionContext ctx, final ISection section)
            throws IOException, SlackApiException {
        // обработчик для кнопки
        final BlockActionPayload.Action action = req.getPayload().getActions().get(0);
        final String actionType = action.getType();
        if ("button".equals(actionType)) {
            getSections().forEach(s -> {
                if (s.getId().equals(action.getBlockId())) {
                    ((AbstractFormField) s.getAccessory()).setValue(action.getValue());
                }
            });
        }
        // сохраняем ID пользователя
        if (Objects.isNull(userId)) {
            userId = req.getPayload().getUser().getId();
        }
        // сохраняем значения всех полей в объекты
        updateViewState(req.getPayload().getView().getState().getValues());
        // вызываем обработчик для кнопки
        if (section.getAccessory() instanceof IHasButtonField) {
            ((IHasButtonField) section.getAccessory()).buttonCallback();
        }
        final View currentView = req.getPayload().getView();
        final ViewsUpdateResponse response = ctx.client()
                                                .viewsUpdate(v -> v
                                                        .hash(currentView.getHash())
                                                        .viewId(currentView.getId())
                                                        .token(BOT_TOKEN)
                                                        .view(getView()));

        if (!response.isOk()) {
            log.error(response.getError());
            log.error("needed {}: ", response.getNeeded());
            log.error("provided {}: ", response.getProvided());
            if (Objects.nonNull(response.getResponseMetadata())) {
                log.error(String.join("\n", response.getResponseMetadata().getMessages()));
            }
        }
    }
}
