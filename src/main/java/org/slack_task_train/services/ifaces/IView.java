package org.slack_task_train.services.ifaces;

import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;

import java.util.List;
import java.util.Map;

public interface IView {
    // возвращает название модалки (сценария)
    String getName();

    // возвращает объект модалки
    View getView();

    // обновляет текущие значения элементов секций модалки
    void updateViewState(Map<String, Map<String, ViewState.Value>> viewState);

    // возвращает список секций модалки
    List<ISection> getSections();

    // возвращает action id модалки
    String getId();

    // возвращает элемент по его id
    IFormField getFieldById(final String id);

    // выполняет регистрацию обработчика обновления данных в форме
    void registerViewUpdate();

    // выполняет регистрацию обработчика завершения заполнения формы
    void registerViewSubmit();

    // возвращает ID пользователя, отправившего форму
    String getUserId();

    // устанавливает ID пользователя вызвавшего модальное окно
    void setUserId(String userId);
}
