package org.slack_task_train.core.task_train;

public class TaskTrainException extends RuntimeException {
    public TaskTrainException(final Throwable e) {
        super(e);
    }

    public TaskTrainException(final String message) {
        super(message);
    }

    public TaskTrainException(final String message, final Throwable e) {
        super(message, e);
    }
}
