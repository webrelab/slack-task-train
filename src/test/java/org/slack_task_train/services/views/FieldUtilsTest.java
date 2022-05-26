package org.slack_task_train.services.views;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slack_task_train.SlackTaskTrainException;

import static org.junit.jupiter.api.Assertions.*;

class FieldUtilsTest {

    @Test
    void optionCutMiddle() {
        final String cutLongText = FieldUtils.optionCut("some long long long text", 15, FieldUtils.CutType.MIDDLE);
        Assertions.assertEquals("some l~~~g text", cutLongText);
        final String cutSmallText = FieldUtils.optionCut("some long long long text", 500, FieldUtils.CutType.MIDDLE);
        Assertions.assertEquals("some long long long text", cutSmallText);
    }

    @Test
    void optionCutEnd() {
        final String cutLongText = FieldUtils.optionCut("some long long long text", 15, FieldUtils.CutType.END);
        Assertions.assertEquals("some long lo~~~", cutLongText);
        final String cutSmallText = FieldUtils.optionCut("some long long long text", 500, FieldUtils.CutType.END);
        Assertions.assertEquals("some long long long text", cutSmallText);
    }
}