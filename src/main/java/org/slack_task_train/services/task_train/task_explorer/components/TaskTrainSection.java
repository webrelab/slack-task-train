package org.slack_task_train.services.task_train.task_explorer.components;

import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.ConfirmationDialogObject;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.ButtonElement;
import lombok.extern.slf4j.Slf4j;
import org.slack_task_train.data.views.base_fields.AbstractButtonFormField;
import org.slack_task_train.data.views.base_sections.AbstractSection;
import org.slack_task_train.services.task_train.TaskTrain;

@Slf4j
public class TaskTrainSection extends AbstractSection {
    private final TaskTrain taskTrain;

    public TaskTrainSection(final TaskTrain taskTrain) {
        super(new Accessory(taskTrain));
        this.taskTrain = taskTrain;
    }

    @Override
    public SectionBlock construct() {
        return SectionBlock.builder()
                           .text(asText(taskTrain.getName(), false))
                           .accessory(getAccessory().getElement())
                           .blockId(getId())
                           .build();
    }

    private static class Accessory extends AbstractButtonFormField {
        private final TaskTrain taskTrain;

        Accessory(final TaskTrain taskTrain) {
            this.taskTrain = taskTrain;
            setId(taskTrain.getUuid());
        }

        @Override
        public BlockElement getElement() {
            return ButtonElement.builder()
                                .actionId(getId())
                                .text(asText("Завершить", false))
                                .confirm(ConfirmationDialogObject
                                        .builder()
                                        .title(asText("Завершение задания", false))
                                        .confirm(asText("Завершить", false))
                                        .deny(asText("Отмена", false))
                                        .text(asText(
                                                "Вы действительно хотите завершить задачу " +
                                                taskTrain.getName() +
                                                "?",
                                                false
                                        ))
                                        .build()
                                )
                                .value("tg" + taskTrain.getUuid())
                                .build();
        }

        @Override
        public String getName() {
            return "Завершение задания " + taskTrain.getName();
        }

        @Override
        public void buttonCallback() {
            taskTrain.stop();
        }
    }
}
