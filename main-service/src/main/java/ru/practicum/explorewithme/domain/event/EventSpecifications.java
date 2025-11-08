package ru.practicum.explorewithme.domain.event;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.domain.request.ParticipationRequest;
import ru.practicum.explorewithme.domain.request.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO попробовать заменить на вариант почище + DTO на вход вместо параметров?
@UtilityClass
public class EventSpecifications {

    // Для поиска евентов по критериям
    public static Specification<Event> publicSearch(
            String text, List<Long> categoryIds, Boolean paid, LocalDateTime start, LocalDateTime end
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> queryPredicates = new ArrayList<>();

            if (start != null && end != null && start.isAfter(end)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }

            // Отбираем только опубликованные евенты
            queryPredicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));

            if (text != null && !text.isBlank()) {
                queryPredicates.add(buildTextSearchPredicate(root, criteriaBuilder, text));
            }

            if (categoryIds != null && !categoryIds.isEmpty()) {
                queryPredicates.add(root.get("category").get("id").in(categoryIds));
            }

            if (paid != null) {
                queryPredicates.add(criteriaBuilder.equal(root.get("paid"), paid));
            }

            if (start != null) {
                queryPredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), start));
            }

            if (end != null) {
                queryPredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), end));
            }

            return criteriaBuilder.and(queryPredicates.toArray(new Predicate[0]));
        };
    }

    // Для подсчёта количества подтверждённых запросов для евента
    public static Specification<Event> onlyAvailable() {
        return (root, query, cb) -> {
            // subquery: count confirmed requests for this event

            // TODO null check ?
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ParticipationRequest> participationRequestRoot = subquery.from(ParticipationRequest.class);
            subquery.select(cb.count(participationRequestRoot));
            subquery.where(
                    cb.equal(participationRequestRoot.get("event"), root),
                    cb.equal(participationRequestRoot.get("status"), RequestStatus.CONFIRMED)
            );
            Expression<Long> confirmed = subquery.getSelection();

            // Либо вообще нет лимита либо подтверждённых заявок меньше лимита
            Predicate unlimited = cb.equal(root.get("participantLimit"), 0);
            Predicate hasSlots = cb.lt(confirmed, root.get("participantLimit"));
            return cb.or(unlimited, hasSlots);
        };
    }

    private static Predicate buildTextSearchPredicate(Root<Event> root, CriteriaBuilder criteriaBuilder, String text) {
        String likePattern = "%" + text.trim().toLowerCase() + "%";
        return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern)
        );
    }
}
