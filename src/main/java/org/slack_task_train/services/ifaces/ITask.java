package org.slack_task_train.services.ifaces;


import org.slack_task_train.services.enums.TaskExecutionStatus;

import java.time.Duration;
import java.util.function.Supplier;

public interface ITask {
    // статус выполнения задания
    TaskExecutionStatus getStatus();

    // статус == SUCCESS
    boolean isTaskComplete();

    // действия перед стартом задачи
    void preExecution();

    // исполняемый код задания
    void executeTask();

    // действия после завершения задачи
    void postExecution();

    // код, который выполняется если предыдущий запуск завершился со статусом FAILED
    void reExecuteTask();

    // функция уменьшения количества попыток
    void decrementAttempts();

    // функция возвращает true если ещё есть попытки
    boolean hasAttempts();

    // будет вызвана в случае исчерпания доступных попыток
    void throwsException();

    // функция сохраняет время последнего запуска задания
    void saveLastUpdateTime();

    // возвращает функцию для рассчёта следующего периода простоя
    Supplier<Long> getIdleDuration();

    // возвращает true если время простоя вышло (то-есть требуется запустить выполнение задания)
    boolean isIdleTimeOut();

    // возвращает количество запусков задания
    int taskExecutionCounter();

    // инкремент количества запусков задания
    void taskExecutionIncrement();

    // возвращает название задания
    String getTaskName();

    String getUuid();

    // возвращает длительность выполнения задачи
    Duration getTaskExecutionDuration();

    // устанавливает время старта исполнения задачи
    void startTime();

    void setStatus(final TaskExecutionStatus status);
}
