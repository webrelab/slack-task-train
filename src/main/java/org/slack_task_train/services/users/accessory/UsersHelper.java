package org.slack_task_train.services.users.accessory;

import lombok.SneakyThrows;
import org.slack_task_train.SlackTaskTrainApp;
import org.slack_task_train.services.ifaces.IRoles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsersHelper {
    @SneakyThrows
    public static List<String> getRoles() {
        final Class<?> rolesClass = Class.forName(SlackTaskTrainApp.slackApp.getConfig().getRoleClass());
        return Stream.of((IRoles[]) rolesClass.getEnumConstants())
                .map(IRoles::getRoleName)
                .collect(Collectors.toList());
    }
}
