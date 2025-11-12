package ru.practicum.explorewithme.dto.event;

public enum EventStateAction {

    PUBLISH_EVENT,  // Публикация
    REJECT_EVENT,   // Отклонение
    SEND_TO_REVIEW, // Отправка ревью
    CANCEL_REVIEW   // Отмена ревью
}
