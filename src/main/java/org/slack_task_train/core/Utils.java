package org.slack_task_train.core;

public class Utils {
    public static void freeze(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new SlackTaskTrainException("Приложение внезапно завершилось", e);
        }
    }
}
