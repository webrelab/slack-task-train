package org.slack_task_train.services.runner;

import com.slack.api.bolt.App;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppRunner {
    private App app;
    public static final String SLACK_BOT_TOKEN = System.getenv( "SLACK_BOT_TOKEN");
    @Bean
    public void init() throws Exception {
        app = new App();
        new SocketModeApp(app).startAsync();
    }

    public App getApp() {
        return app;
    }
}
