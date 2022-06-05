package org.slack_task_train;

import org.slack_task_train.core.runner.AppRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class App {
    public static AppRunner slackApp;

    public static void main(String[] args) throws Exception{
        try (final ConfigurableApplicationContext context = SpringApplication.run(App.class, args)) {
            slackApp = context.getBean(AppRunner.class);
            slackApp.getServiceRegistration().init();
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}
