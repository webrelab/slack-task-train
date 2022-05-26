package org.slack_task_train.services.views;

import com.slack.api.bolt.App;
import com.slack.api.model.block.composition.OptionObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.services.runner.AppRunner;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ElementTest {
    private final Element element;

    public ElementTest() {
        AppRunner appRunner = Mockito.mock(AppRunner.class);
        App app = Mockito.spy(App.class);
        Mockito.when(appRunner.getApp()).thenReturn(app);
        SlackTaskTrainApp.slackApp = appRunner;
        element = new Element();
    }

    @Test
    void asText() {
        Assertions.assertEquals("some text", element.asText("some text", true).getText());
        Assertions.assertEquals(
                "plain_text",
                element.asText("some text", true).getType()
        );
    }

    @Test
    void asMiddleCutText() {
        Assertions.assertEquals(
                "some text some tex~~~ome text some text",
                element.asMiddleCutText("some text some text some text some text some text", 40, true).getText()
        );
        Assertions.assertEquals(
                "plain_text",
                element.asMiddleCutText("some text", 40,true).getType()
        );
    }

    @Test
    void asMrkdn() {
        Assertions.assertEquals(
                "some text",
                element.asMrkdn("some text").getText()
        );
        Assertions.assertEquals(
                "mrkdwn",
                element.asMrkdn("some text").getType()
        );
    }

    @Test
    void asViewTitle() {
        Assertions.assertEquals("some text", element.asViewTitle("some text", true).getText());
        Assertions.assertEquals(
                "plain_text",
                element.asViewTitle("some text", true).getType()
        );
    }

    @Test
    void asViewClose() {
        Assertions.assertEquals("some text", element.asViewClose("some text", true).getText());
        Assertions.assertEquals(
                "plain_text",
                element.asViewClose("some text", true).getType()
        );
    }

    @Test
    void asViewSubmit() {
        Assertions.assertEquals("some text", element.asViewSubmit("some text", true).getText());
        Assertions.assertEquals(
                "plain_text",
                element.asViewSubmit("some text", true).getType()
        );
    }

    @Test
    void asOptionObject() {
        final OptionObject optionObject = element.asOptionObject("some text", "some_value");
        Assertions.assertEquals("some text", optionObject.getText().getText());
        Assertions.assertEquals("some_value", optionObject.getValue());
    }

    @Test
    void AsOptionObjectWithDescription() {
        final OptionObject optionObject = element.asOptionObject("some text", "some_value", "some description");
        Assertions.assertEquals("some text", optionObject.getText().getText());
        Assertions.assertEquals("some_value", optionObject.getValue());
        Assertions.assertEquals("some description", optionObject.getDescription().getText());
    }
}