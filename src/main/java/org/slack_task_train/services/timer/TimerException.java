package org.slack_task_train.services.timer;

public class TimerException extends RuntimeException {
    private static final long serialVersionUID = 898283741307845110L;

    public TimerException(final String message) {
        super(message);
    }

    public TimerException(final Throwable e) {
        super(e);
    }
}
