package org.slack_task_train.core.task_train;

import com.slack.api.RequestConfigurator;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.slack_task_train.App;
import org.slack_task_train.core.Utils;
import org.slack_task_train.core.enums.TaskExecutionStatus;
import org.slack_task_train.core.ifaces.ITask;
import org.slack_task_train.core.runner.AppRunner;
import org.slack_task_train.core.timer.Timer;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class TaskTrainTest {

    TaskTrain taskTrain;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void beforeEach() throws SlackApiException, IOException {
        AppRunner appRunner = Mockito.mock(AppRunner.class);
        com.slack.api.bolt.App app = Mockito.mock(com.slack.api.bolt.App.class);
        Mockito.when(appRunner.getApp()).thenReturn(app);
        App.slackApp = appRunner;
        ChatPostMessageResponse response = Mockito.mock(ChatPostMessageResponse.class);
        MethodsClient methodsClient = Mockito.mock(MethodsClient.class);
        Mockito.when(app.getClient()).thenReturn(methodsClient);
        Mockito.when(app.getClient().chatPostMessage((RequestConfigurator<ChatPostMessageRequest.ChatPostMessageRequestBuilder>) Mockito.any())).thenReturn(response);
        Mockito.when(response.isOk()).thenReturn(true);
        taskTrain = new TaskTrain("test task train", "");
    }

    @AfterEach
    public void afterEach() {
        taskTrain.stop();
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
    }

    @Test
    public void checkQueueStartConditionWhenFalse() {
        ExecutionTask task = new ExecutionTask();
        taskTrain.add(task)
                .addQueueStartCondition(() -> false)
                .addQueueCompleteCondition(task::isTaskComplete)
                .execute();
        Utils.freeze(100);
        Assertions.assertEquals(TaskExecutionStatus.NOT_STARTED, task.getStatus());
    }

    @Test
    public void checkQueueStartConditionWhenTrue() {
        ExecutionTask task1 = new ExecutionTask();
        ExecutionTask task2 = new ExecutionTask();
        ExecutionTask task3 = new ExecutionTask();
        taskTrain.add(task1)
                .addQueueStartCondition(() -> taskTrain.getQueue().size() == 3)
                .addQueueCompleteCondition(task3::isTaskComplete)
                .execute();
        Utils.freeze(100);
        Assertions.assertEquals(TaskExecutionStatus.NOT_STARTED, task1.getStatus());
        taskTrain.add(task2, task1, true, task1::isTaskComplete);
        Utils.freeze(1200);
        Assertions.assertEquals(TaskExecutionStatus.NOT_STARTED, task1.getStatus());
        taskTrain.add(task3, task2, true, task2::isTaskComplete);
        Utils.freeze(2200);
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, task1.getStatus());
    }

    @Test
    public void createProtectedTaskTrain() {
        TaskTrain protectedTaskTrain = new TaskTrain("protectedTaskTrain", "", true);
        Assertions.assertTrue(protectedTaskTrain.isProtected());
    }

    @Test
    public void attachSeveralTasksToOneDependent() {
        ExecutionTask task1 = new ExecutionTask();
        ExecutionTask task2 = new ExecutionTask();
        ExecutionTask task3 = new ExecutionTask();
        taskTrain.add(task1)
                .add(task2)
                .add(task3, task1, true, task1::isTaskComplete)
                .add(task3, task2, true, task2::isTaskComplete);
        Assertions.assertEquals(3, taskTrain.getQueue().size());
        Optional<TaskTrain.Dependent> task3DependentOptional = taskTrain.getQueue().stream().filter(d -> d.getDependent().equals(task3)).findFirst();
        Assertions.assertTrue(task3DependentOptional.isPresent());
        List<ITask> sources = task3DependentOptional.get().getSources().stream().map(TaskTrain.Source::getSource).collect(Collectors.toList());
        Assertions.assertTrue(sources.contains(task1));
        Assertions.assertTrue(sources.contains(task2));
    }

    @Test
    public void fullTaskPlay() {
        ExecutionTask task1 = new ExecutionTask();
        ExecutionTask task2 = new ExecutionTask();
        ExecutionTask task3 = new ExecutionTask();
        taskTrain.add(task1)
                .add(task2, task1, true, task1::isTaskComplete)
                .add(task3, task2, true, task2::isTaskComplete)
                .addQueueCompleteCondition(task3::isTaskComplete);
        Assertions.assertEquals(3, taskTrain.getQueue().size());
        taskTrain.execute();
        Timer.executeTimer(12, () -> taskTrain.getQueue().isEmpty());
        Assertions.assertEquals(0, taskTrain.getQueue().size());
        Assertions.assertTrue(taskTrain.isComplete());
        Assertions.assertFalse(taskTrain.isActive());
    }

    @Test
    public void notStartedTaskDontUpdateTaskTime() {
        ExecutionTask task = new ExecutionTask();
        Assertions.assertEquals(0L, task.getTaskExecutionDuration().getSeconds());
        taskTrain.add(task, task, true, () -> false)
                .addQueueCompleteCondition(task::isTaskComplete)
                .execute();
        Utils.freeze(2000);
        Assertions.assertEquals(0L, task.getTaskExecutionDuration().getSeconds());
    }

    @Test
    public void updateTaskExecutionDuration() {
        RepeatableTask task = new RepeatableTask();
        Assertions.assertEquals(0L, task.getTaskExecutionDuration().getSeconds());
        taskTrain.add(task)
                .addQueueCompleteCondition(task::isTaskComplete)
                .execute();
        Utils.freeze(4000);
        Assertions.assertTrue(
                task.getTaskExecutionDuration().getSeconds() > 2,
                () -> String.format("Текущая продолжительность %ss, ожидаемая > 2s", task.getTaskExecutionDuration().getSeconds())
        );
    }

    @Test
    public void taskDontBeNull() {
        PausedTask task = new PausedTask();
        Assertions.assertThrows(NullPointerException.class, () -> taskTrain.add(null));
        Assertions.assertThrows(NullPointerException.class, () -> taskTrain.add(task, null, true, () -> true));
        Assertions.assertThrows(NullPointerException.class, () -> taskTrain.add(null, task, true, () -> true));
    }

    @Test
    public void autoStopTaskTrainWhenThrow10Times() {
        ThrowedTask task = new ThrowedTask();
        taskTrain.setIdleConveyorMillis(10)
                .add(task)
                .addQueueCompleteCondition(task::isTaskComplete);
        Assertions.assertDoesNotThrow(taskTrain::execute);
        Timer.executeTimer(30, taskTrain::isComplete);
        Assertions.assertTrue(taskTrain.isComplete());
    }
}