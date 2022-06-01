package org.slack_task_train.services.task_train;

import com.slack.api.RequestConfigurator;
import com.slack.api.bolt.App;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.SlackTaskTrainException;
import org.slack_task_train.Utils;
import org.slack_task_train.services.enums.TaskExecutionStatus;
import org.slack_task_train.services.runner.AppRunner;
import org.springframework.util.Assert;

import java.io.IOException;

class TaskTrainTest {

    TaskTrain taskTrain;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void beforeEach() throws SlackApiException, IOException {
        AppRunner appRunner = Mockito.mock(AppRunner.class);
        App app = Mockito.mock(App.class);
        Mockito.when(appRunner.getApp()).thenReturn(app);
        SlackTaskTrainApp.slackApp = appRunner;
        ChatPostMessageResponse response = Mockito.mock(ChatPostMessageResponse.class);
        MethodsClient methodsClient = Mockito.mock(MethodsClient.class);
        Mockito.when(app.getClient()).thenReturn(methodsClient);
        Mockito.when(app.getClient().chatPostMessage((RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder>) Mockito.any())).thenReturn(response);
        Mockito.when(response.isOk()).thenReturn(true);
        taskTrain = new TaskTrain("test task train", "");
    }

    @Test
    public void preExecutionTask() {
        PreExecutionTask task = new PreExecutionTask();
        taskTrain.add(task)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertFalse(task.getPreExecution());
        taskTrain.execute();
        Utils.freeze(100);
        Assertions.assertTrue(task.getPreExecution());
    }

    @Test
    public void executionTask() {
        ExecutionTask task = new ExecutionTask();
        taskTrain.add(task)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertFalse(task.getExecution());
        taskTrain.execute();
        Utils.freeze(100);
        Assertions.assertTrue(task.getExecution());
    }

    @Test
    public void postExecutionTask() {
        PostExecutionTask task = new PostExecutionTask();
        taskTrain.add(task)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertFalse(task.getPostExecution());
        taskTrain.execute();
        Utils.freeze(1100);
        Assertions.assertTrue(task.getPostExecution());
    }

    @Test
    public void changeStatusToSuccess() {
        ChangeStatusTask task = new ChangeStatusTask(TaskExecutionStatus.SUCCESS);
        taskTrain.add(task)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertEquals(TaskExecutionStatus.NOT_STARTED, task.getStatus());
        taskTrain.execute();
        Utils.freeze(100);
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, task.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = TaskExecutionStatus.class, names = {"IN_PROGRESS", "FAILED"})
    public void changeStatusToInProgressOrFailed(TaskExecutionStatus status) {
        ChangeStatusTask task = new ChangeStatusTask(status);
        taskTrain.add(task)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertEquals(TaskExecutionStatus.NOT_STARTED, task.getStatus());
        taskTrain.execute();
        Utils.freeze(2100);
        Assertions.assertEquals(status, task.getStatus());
        Assertions.assertTrue(taskTrain.isActive());
        Assertions.assertFalse(taskTrain.isComplete());
        taskTrain.stop();
    }


    @Test
    public void changeStatusToRepeatable() {
        RepeatableTask task = new RepeatableTask();
        taskTrain.add(task)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertEquals(TaskExecutionStatus.NOT_STARTED, task.getStatus());
        taskTrain.execute();
        Utils.freeze(100);
        Assertions.assertEquals(TaskExecutionStatus.REPEATABLE, task.getStatus());
        taskTrain.stop();
    }

    @Test
    public void checkIdleDurationForRepeatableTask() {
        ChangeStatusTask task = new ChangeStatusTask(TaskExecutionStatus.REPEATABLE);
        Assertions.assertThrows(TaskTrainException.class, task::isIdleTimeOut);
    }

    @Test
    public void checkQueueCompleteConditionThrowing() {
        ExecutionTask task = new ExecutionTask();
        taskTrain.add(task);
        Assertions.assertThrows(TaskTrainException.class, taskTrain::execute);
    }

    @Test
    public void getQueue() {
        PausedTask pausedTask = new PausedTask();
        ExecutionTask task = new ExecutionTask();
        taskTrain.add(pausedTask)
                .add(task, pausedTask, true, pausedTask::isTaskComplete)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertEquals(2, taskTrain.getQueue().size());
        taskTrain.execute();
        Utils.freeze(1000);
        Assertions.assertEquals(2, taskTrain.getQueue().size());
        taskTrain.stop();
    }
}