package org.slack_task_train;

public class SlackTaskTrainException extends RuntimeException {

    public SlackTaskTrainException() {
        super();
    }

    public SlackTaskTrainException(final String message) {
        super(message);
    }

    public SlackTaskTrainException(final String message, final Throwable e) {
        super(message, e);
    }

    public SlackTaskTrainException(final Throwable e) {
        super(e);
    }
}
