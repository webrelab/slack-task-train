package org.slack_task_train.data.repositories;

import org.slack_task_train.data.models.UserRolesModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRolesRepository extends CrudRepository<UserRolesModel, Integer> {
    List<UserRolesModel> findByUserId(String userId);
}
