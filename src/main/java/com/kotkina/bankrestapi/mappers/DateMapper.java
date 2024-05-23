package com.kotkina.bankrestapi.mappers;

import org.mapstruct.Mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public class DateMapper {

    private static final String DATE_PATTERN_FORMAT = "yyyy-MM-dd";

    public String asString(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN_FORMAT)
                .withZone(ZoneId.systemDefault());

        return localDate != null ? formatter.format(localDate) : null;
    }
}
