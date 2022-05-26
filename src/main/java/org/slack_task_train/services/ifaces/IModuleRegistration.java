package org.slack_task_train.services.ifaces;

import com.slack.api.model.block.LayoutBlock;
import org.slack_task_train.services.enums.SlackRoles;
import org.slack_task_train.services.enums.StartSection;

public interface IModuleRegistration {

    // возвращает блок с описанием элемент кнопки, по которой вызывается модальное окно запуска задачи
    LayoutBlock getStartButton();

    // выполняет регистрацию обработчиков нажатия кнопки
    void registerStartButton();

    // возвращает ID кнопки вызова сценария
    String getButtonId();

    // список ролей для которых этот модуль будет доступен
    SlackRoles[] acceptedRoles();

    // название модуля
    String getName();

    // название секции
    StartSection getStartSection();

    // для сервисов, которые должны стартовать автоматически с ботом
    boolean isDemon();

    // класс для запуска сервиса
    IDemon getDemon();
}
