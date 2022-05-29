package org.slack_task_train.data.services;

import org.slack_task_train.data.models.UserRolesModel;
import org.slack_task_train.data.repositories.UserRolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserRolesService {
    @Autowired
    private UserRolesRepository repository;

    public List<UserRolesModel> getRolesByUserId(final String userId) {
        return repository.findByUserId(userId);
    }

    public UserRolesRepository getRepository() {
        return repository;
    }
}
