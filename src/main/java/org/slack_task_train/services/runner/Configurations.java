package org.slack_task_train.services.runner;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "slack-task-train")
public class Configurations {
    private String modulePackages;
    private String roleClass;
    private String serviceMessageChannel;
}
