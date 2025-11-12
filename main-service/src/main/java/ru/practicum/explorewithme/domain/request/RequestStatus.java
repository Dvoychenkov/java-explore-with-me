package ru.practicum.explorewithme.domain.request;

public enum RequestStatus {

    PENDING,   // Ожидает рассмотрения
    CONFIRMED, // Одобрена
    REJECTED,  // Отклонена организатором
    CANCELED   // Отменена пользователем
}
