package ru.practicum.explorewithme.domain.event;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.practicum.explorewithme.domain.request.ParticipationRequest;
import ru.practicum.explorewithme.domain.request.RequestStatus;
import ru.practicum.explorewithme.dto.event.AdminEventSearchCriteriaDto;
import ru.practicum.explorewithme.dto.event.PublicEventSearchCriteriaDto;
import ru.practicum.explorewithme.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class EventSpecifications {

    private static final String TITLE_FIELD = "title";
    private static final String ANNOTATION_FIELD = "annotation";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String STATE_FIELD = "state";
    private static final String CATEGORY_FIELD = "category";
    private static final String PAID_FIELD = "paid";
    private static final String EVENT_DATE_FIELD = "eventDate";
    private static final String PARTICIPANT_LIMIT_FIELD = "participantLimit";
    private static final String EVENT_FIELD = "event";
    private static final String STATUS_FIELD = "status";
    private static final String INITIATOR_FIELD = "initiator";
    private static final String ID_FIELD = "id";

    // Для поиска евентов по критериям
    public static Specification<Event> publicSearch(PublicEventSearchCriteriaDto publicEventSearchCriteriaDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> queryPredicates = new ArrayList<>();

            DateTimeUtils.validateDateRange(
                    publicEventSearchCriteriaDto.getRangeStart(), publicEventSearchCriteriaDto.getRangeEnd());

            // Отбираем только опубликованные евенты
            queryPredicates.add(criteriaBuilder.equal(root.get(STATE_FIELD), EventState.PUBLISHED));


            String text = publicEventSearchCriteriaDto.getText();
            if (StringUtils.hasText(text)) {
                queryPredicates.add(
                        buildTextSearchPredicate(root, criteriaBuilder, text));
            }

            List<Long> categories = publicEventSearchCriteriaDto.getCategories();
            if (!CollectionUtils.isEmpty(categories)) {
                queryPredicates.add(root.get(CATEGORY_FIELD).get(ID_FIELD).in(categories));
            }

            Boolean paid = publicEventSearchCriteriaDto.getPaid();
            if (paid != null) {
                queryPredicates.add(criteriaBuilder.equal(root.get(PAID_FIELD), paid));
            }

            LocalDateTime rangeStart = publicEventSearchCriteriaDto.getRangeStart();
            if (rangeStart != null) {
                queryPredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(EVENT_DATE_FIELD), rangeStart));
            }

            LocalDateTime rangeEnd = publicEventSearchCriteriaDto.getRangeEnd();
            if (rangeEnd != null) {
                queryPredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(EVENT_DATE_FIELD), rangeEnd));
            }

            return criteriaBuilder.and(queryPredicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> adminSearch(AdminEventSearchCriteriaDto adminEventSearchCriteriaDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> queryPredicates = new ArrayList<>();

            DateTimeUtils.validateDateRange(
                    adminEventSearchCriteriaDto.getRangeStart(), adminEventSearchCriteriaDto.getRangeEnd());

            List<Long> users = adminEventSearchCriteriaDto.getUsers();
            if (users != null && !users.isEmpty()) {
                queryPredicates.add(root.get(INITIATOR_FIELD).get(ID_FIELD).in(users));
            }

            List<EventState> states = adminEventSearchCriteriaDto.getStates();
            if (states != null && !states.isEmpty()) {
                queryPredicates.add(root.get(STATE_FIELD).in(states));
            }

            List<Long> categories = adminEventSearchCriteriaDto.getCategories();
            if (categories != null && !categories.isEmpty()) {
                queryPredicates.add(root.get(CATEGORY_FIELD).get(ID_FIELD).in(categories));
            }

            LocalDateTime rangeStart = adminEventSearchCriteriaDto.getRangeStart();
            if (rangeStart != null) {
                queryPredicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(EVENT_DATE_FIELD), rangeStart));
            }

            LocalDateTime rangeEnd = adminEventSearchCriteriaDto.getRangeEnd();
            if (rangeEnd != null) {
                queryPredicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(EVENT_DATE_FIELD), rangeEnd));
            }

            return criteriaBuilder.and(queryPredicates.toArray(new Predicate[0]));
        };
    }

    // Для подсчёта количества подтверждённых запросов для евента
    public static Specification<Event> onlyAvailable() {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return criteriaBuilder.conjunction();
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ParticipationRequest> participationRequestRoot = subquery.from(ParticipationRequest.class);
            subquery.select(criteriaBuilder.count(participationRequestRoot));
            subquery.where(
                    criteriaBuilder.equal(participationRequestRoot.get(EVENT_FIELD), root),
                    criteriaBuilder.equal(participationRequestRoot.get(STATUS_FIELD), RequestStatus.CONFIRMED)
            );
            Expression<Long> confirmed = subquery.getSelection();

            // Либо вообще нет лимита либо подтверждённых заявок меньше лимита
            Predicate unlimited = criteriaBuilder.equal(root.get(PARTICIPANT_LIMIT_FIELD), 0);
            Predicate hasSlots = criteriaBuilder.lt(confirmed, root.get(PARTICIPANT_LIMIT_FIELD));

            return criteriaBuilder.or(unlimited, hasSlots);
        };
    }

    public static Specification<Event> hasEventsInCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (categoryId == null) {
                return null;
            }

            return criteriaBuilder.equal(root.get(CATEGORY_FIELD).get(ID_FIELD), categoryId);
        };
    }

    private static Predicate buildTextSearchPredicate(Root<Event> root, CriteriaBuilder criteriaBuilder, String text) {
        String likePattern = "%" + text.trim().toLowerCase() + "%";

        return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get(TITLE_FIELD)), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(ANNOTATION_FIELD)), likePattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(DESCRIPTION_FIELD)), likePattern)
        );
    }
}
