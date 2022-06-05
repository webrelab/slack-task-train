package org.slack_task_train.core.users.accessory;

import lombok.SneakyThrows;
import org.slack_task_train.App;
import org.slack_task_train.core.ifaces.IRoles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UsersHelper {
    @SneakyThrows
    public static List<String> getRoles() {
        final Class<?> rolesClass = Class.forName(App.slackApp.getConfig().getRoleClass());
        return Stream.of((IRoles[]) rolesClass.getEnumConstants())
                .map(IRoles::getRoleName)
                .collect(Collectors.toList());
    }
}
