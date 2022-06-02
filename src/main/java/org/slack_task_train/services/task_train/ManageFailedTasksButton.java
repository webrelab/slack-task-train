package org.slack_task_train.services.task_train;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.ButtonElement;
import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.data.views.Element;
import org.slack_task_train.services.enums.TaskExecutionStatus;
import org.slack_task_train.services.ifaces.IReset;
import org.slack_task_train.services.ifaces.ITask;

import java.io.IOException;
import java.util.*;

@Slf4j
public class ManageFailedTasksButton extends Element {
    private final Map<ITask, String> taskToButtonIdMap = new HashMap<>();
    private final Map<String, TaskTrain.Dependent> buttonIdToDependentMap = new HashMap<>();
    private final List<LayoutBlock> sectionBlocks = new ArrayList<>();
    private final TaskTrain taskTrain;

    public ManageFailedTasksButton(final TaskTrain taskTrain) {
        this.taskTrain = taskTrain;
    }

    public void postMessage(final TaskTrain.Dependent dependent, final String userId)
            throws IOException, SlackApiException {
        addErrorMessage(dependent);
        addSkipStageButton(dependent);
        collectSections(dependent);
        addInterruptScenarioButton();
        final ChatPostMessageResponse response = APP.getClient().chatPostMessage(r -> r
                .token(BOT_TOKEN)
                .channel(userId)
                .text("Выберите действия для продолжения работы")
                .blocks(sectionBlocks)
        );
        if (!response.isOk()) {
            log.error(response.toString());
        }
    }

    private void addErrorMessage(final TaskTrain.Dependent dependent) {
        final SectionBlock errorSection = SectionBlock
                .builder()
                .text(asMrkdn("*У нас есть проблема.* Задание '" +
                             dependent.getDependent().getTaskName() +
                             "' провалилось, что будешь делать?"))
                .build();
        sectionBlocks.add(errorSection);
    }

    private void addSkipStageButton(final TaskTrain.Dependent dependent) {
        final String skipUuid = UUID.randomUUID().toString();
        final ButtonElement button = ButtonElement
                .builder()
                .actionId(skipUuid)
                .text(asText("Пропустить", false))
                .value(skipUuid)
                .build();
        final SectionBlock skipStageSection = SectionBlock
                .builder()
                .blockId("block_" + skipUuid)
                .accessory(button)
                .text(asText("Зачесть задание '" +
                             dependent.getDependent().getTaskName() +
                             "' как выполненное и продолжить со следующего шага", false))
                .build();
        sectionBlocks.add(skipStageSection);
        APP.blockAction(skipUuid, (req, ctx) -> {
            dependent.setPostExecutionDone(true);
            taskTrain.getQueue()
                    .forEach(d -> d
                            .getSources()
                            .stream()
                            .filter(s -> s.getSource().equals(dependent.getDependent()))
                            .forEach(TaskTrain.Source::clearCondition)
                    );
            final ChatUpdateResponse chatUpdateResponse = ctx.client().chatUpdate(u -> u
                    .blocks(new ArrayList<>())
                    .text("*Ну окей, я закрыл глаза на провал задания '" +
                          dependent.getDependent().getTaskName() +
                          "'.* _Но я не буду делать это вечно, сделайте уже что бы всё работало!_ :smirk_cat:")
                    .ts(req.getPayload().getMessage().getTs())
                    .channel(req.getPayload().getChannel().getId())
                    .token(BOT_TOKEN)
            );
            if (!chatUpdateResponse.isOk()) {
                log.error(chatUpdateResponse.toString());
            }
            return ctx.ack();
        });
    }

    private void addInterruptScenarioButton() {
        final String interruptUuid = UUID.randomUUID().toString();
        final ButtonElement button = ButtonElement
                .builder()
                .actionId(interruptUuid)
                .text(asText("Прервать", false))
                .style("danger")
                .value(interruptUuid)
                .build();
        final SectionBlock skipStageSection = SectionBlock
                .builder()
                .blockId("block_" + interruptUuid)
                .accessory(button)
                .text(asText("Прервать выполнение всего сценария", false))
                .build();
        sectionBlocks.add(skipStageSection);
        APP.blockAction(interruptUuid, (req, ctx) -> {
            taskTrain.stop();
            final ChatUpdateResponse chatUpdateResponse = APP.client().chatUpdate(r -> r
                    .blocks(new ArrayList<>())
                    .text("*Исполнено! сценарий остановлен*. _У тебя осталось ещё два желания_ :male_mage:")
                    .ts(req.getPayload().getMessage().getTs())
                    .channel(req.getPayload().getChannel().getId())
                    .token(BOT_TOKEN)
            );
            if (!chatUpdateResponse.isOk()) {
                log.error(chatUpdateResponse.toString());
            }
            return ctx.ack();
        });
    }

    private void collectSections(final TaskTrain.Dependent dependent) {
        generateButtonId(dependent);
        final SectionBlock dependentSection = SectionBlock
                .builder()
                .blockId("block_" +
                         taskToButtonIdMap.get(dependent.getDependent()))
                .accessory(getButton(dependent.getDependent()))
                .text(asText(
                        "Выполнить перезапуск начиная с задачи '" + dependent.getDependent().getTaskName() + "'",
                        false
                ))
                .build();
        sectionBlocks.add(dependentSection);
        // регистрация обработчика нажатия на кнопку
        APP.blockAction(taskToButtonIdMap.get(dependent.getDependent()), (req, ctx) -> {
            final BlockActionPayload.Action action = req.getPayload().getActions().get(0);
            if ("button".equals(action.getType())) {
                resetTasksState(buttonIdToDependentMap.get(action.getValue()));

                final ChatUpdateResponse chatUpdateResponse = ctx.client().chatUpdate(r -> r
                        .blocks(new ArrayList<>())
                        .text(
                                "*Исполнено! Все задачи перезапущены начиная с '" +
                                buttonIdToDependentMap.get(action.getValue()).getDependent().getTaskName() +
                                "'*. _Может пока по пиву?_ :beers:")
                        .channel(req.getPayload().getChannel().getId())
                        .ts(req.getPayload().getMessage().getTs())
                        .token(BOT_TOKEN)
                );

                if (!chatUpdateResponse.isOk()) {
                    log.error(chatUpdateResponse.toString());
                }
            }
            return ctx.ack();
        });
        dependent.getSources().forEach(source -> {
            taskTrain.getQueue()
                    .stream()
                    .filter(d -> d.getDependent().equals(source.getSource()))
                    .findFirst()
                    .ifPresent(this::collectSections);
        });
    }

    private BlockElement getButton(final ITask task) {
        return ButtonElement.builder()
                            .text(asText("Перезапустить", false))
                            .actionId(taskToButtonIdMap.get(task))
                            .value(taskToButtonIdMap.get(task))
                            .build();
    }

    private void generateButtonId(final TaskTrain.Dependent dependent) {
        taskToButtonIdMap.put(dependent.getDependent(), UUID.randomUUID().toString());
        buttonIdToDependentMap.put(taskToButtonIdMap.get(dependent.getDependent()), dependent);
    }

    private void resetTasksState(final TaskTrain.Dependent dependent) {
        dependent.getDependent().setStatus(TaskExecutionStatus.NOT_STARTED);
        dependent.setPostExecutionDone(false);
        if (dependent.getDependent() instanceof IReset) {
            ((IReset) dependent.getDependent()).reset();
        }
        taskTrain.getQueue()
                .stream()
                .filter(d -> d.getSources().stream().anyMatch(s -> s.getSource().equals(dependent.getDependent())))
                .forEach(this::resetTasksState);
    }

}
