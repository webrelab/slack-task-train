package org.slack_task_train.services.ifaces;

import com.slack.api.model.block.LayoutBlock;

public interface IModuleRegistration {

    // возвращает блок с описанием элемент кнопки, по которой вызывается модальное окно запуска задачи
    LayoutBlock getStartButton();

    // выполняет регистрацию обработчиков нажатия кнопки
    void registerStartButton();

    // возвращает ID кнопки вызова сценария
    String getButtonId();

    // список ролей для которых этот модуль будет доступен
    IRoles[] acceptedRoles();

    // название модуля
    String getName();

    // название секции
    ICategory getCategory();

    // для сервисов, которые должны стартовать автоматически с ботом
    boolean isDemon();

    // класс для запуска сервиса
    IDemon getDemon();
}
