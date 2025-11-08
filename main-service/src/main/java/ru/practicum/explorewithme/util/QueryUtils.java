package ru.practicum.explorewithme.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public final class QueryUtils {

    public Pageable offsetLimit(int from, int size, Sort sort) {
        int page = from / Math.max(size, 1);

        return PageRequest.of(page, size,
                (sort == null) ?
                        Sort.unsorted() :
                        sort);
    }
}
