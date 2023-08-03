package ru.practicum.dto;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class GetEventParametersDto {
    List<Long> users;
    List<Status> states;
    List<Long> categories;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    Integer from;
    Integer size;
    String text;
    List<Long> categoryIds;
    Boolean paid;
    Boolean onlyAvailable;
    String sort;
}
