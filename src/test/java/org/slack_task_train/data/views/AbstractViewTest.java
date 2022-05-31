package org.slack_task_train.data.views;

import com.slack.api.bolt.App;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.services.ifaces.IDispatcher;
import org.slack_task_train.services.ifaces.IFormField;
import org.slack_task_train.services.ifaces.ISection;
import org.slack_task_train.services.runner.AppRunner;
import org.slack_task_train.example.accessory.PlainTextSection;

import java.util.*;

class AbstractViewTest {

    private static final String NAME = "test name";

    private final AbstractView<TestDispatcher> abstractView;
    private static class TestDispatcher implements IDispatcher {
        @Override
        public void dispatch() {}
    }

    public AbstractViewTest() {
        AppRunner appRunner = Mockito.mock(AppRunner.class);
        App app = Mockito.spy(App.class);
        Mockito.when(appRunner.getApp()).thenReturn(app);
        SlackTaskTrainApp.slackApp = appRunner;
        abstractView = new AbstractView<>() {
            public final PlainTextSection plainTextSection1 = new PlainTextSection();
            public final PlainTextSection plainTextSection2 = new PlainTextSection(
                    true,
                    () -> true
            );

            @Override
            public String getName() {
                return NAME;
            }

            @Override
            public void registerViewSubmit() {

            }
        };
    }

    @Test
    void viewBeenGenerated() {
        Assertions.assertNotNull(abstractView.getView());
    }

    @Test
    void whenUpdateViewStateWithNonEmptyValue() {
        ViewState.Value value = Mockito.mock(ViewState.Value.class);
        Mockito.when(value.getValue()).thenReturn("some value");
        Map<String, Map<String, ViewState.Value>> map = new HashMap<>();
        String firstSectionId = abstractView.getSections().get(0).getId();
        String accessoryId = abstractView.getSections().get(0).getAccessory().getId();
        map.put(firstSectionId, new HashMap<>());
        map.get(firstSectionId).put(accessoryId, value);
        Assertions.assertDoesNotThrow(() -> abstractView.updateViewState(map));
        Assertions.assertEquals(
                abstractView.getSections().get(0).getAccessory().getValue(), "some value"
        );
   }

    @Test
    void sectionsBeCollected() {
        final List<ISection> sections = abstractView.getSections();
        Assertions.assertAll(
                () -> Assertions.assertEquals(sections.get(0).getClass(), PlainTextSection.class),
                () -> Assertions.assertEquals(sections.get(1).getClass(), PlainTextSection.class)
        );
    }

    @Test
    void blocksBeCollectedWithDivider() {
        final List<LayoutBlock> blocks = abstractView.getBlocks();
        Assertions.assertAll(
                () -> Assertions.assertEquals(blocks.get(0).getType(), "input"),
                () -> Assertions.assertEquals(blocks.get(1).getType(), "divider"),
                () -> Assertions.assertEquals(blocks.get(2).getType(), "input"),
                () -> Assertions.assertEquals(blocks.get(3).getType(), "divider")
        );
    }

    @Test
    void viewHasFields() {
        final View view = abstractView.getView();
        Assertions.assertAll(
                () -> Assertions.assertEquals(view.getType(), "modal"),
                () -> Assertions.assertEquals(view.getTitle().getText(), NAME),
                () -> Assertions.assertNotEquals(view.getCallbackId(), ""),
                () -> Assertions.assertEquals(view.getClose().getText(), "Отмена"),
                () -> Assertions.assertEquals(view.getSubmit().getText(), "Запуск"),
                () -> Assertions.assertEquals(view.getBlocks().size(), 4)
        );
    }

    @Test
    void modalHasId() {
        Assertions.assertNotNull(abstractView.getId());
    }

    @Test
    void getAccessoryBiId() {
        final IFormField field1 = abstractView.getFieldById(abstractView.getSections().get(0).getAccessory().getId());
        final IFormField field2 = abstractView.getFieldById(abstractView.getSections().get(1).getAccessory().getId());
        Assertions.assertAll(
                () -> Assertions.assertNotNull(field1),
                () -> Assertions.assertNotNull(field2)
        );
        Assertions.assertNotEquals(field1, field2);
    }

    @Test
    void registerViewUpdate() {
        final AbstractView spy = Mockito.spy(abstractView);
        List<ISection> sections = Arrays.asList(new PlainTextSection(), new PlainTextSection());
        Mockito.when(spy.getSections()).thenReturn(sections);
        spy.registerViewUpdate();
        Mockito.verify(spy).getSections();
        Mockito.verify(spy, Mockito.times(1)).registerViewUpdate(sections.get(0));
        Mockito.verify(spy, Mockito.times(1)).registerViewUpdate(sections.get(1));
    }

    @Test
    void setAndGetUserId() {
        final String userId = UUID.randomUUID().toString();
        abstractView.setUserId(userId);
        Assertions.assertEquals(userId, abstractView.getUserId());
    }

    @Test
    void registerViewSubmit() {
        Assertions.assertDoesNotThrow(() -> abstractView.registerViewSubmit(() -> {}));
    }

}
