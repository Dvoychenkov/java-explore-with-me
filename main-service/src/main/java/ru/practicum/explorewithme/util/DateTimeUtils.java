package ru.practicum.explorewithme.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateTimeUtils {

    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_TIME_FORMAT);

    public static String toString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }

        return dateTime.format(ISO_DATE_TIME_FORMATTER);
    }

    public static LocalDateTime fromString(String possibleDateTime) {
        if (possibleDateTime == null || possibleDateTime.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(possibleDateTime, ISO_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(String.format("Date time string must match format '%s'", ISO_DATE_TIME_FORMAT));
        }
    }


    public static void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && (start.isAfter(end) || start.equals(end))) {
            throw new IllegalArgumentException("Start date must be after end date");
        }
    }
}
