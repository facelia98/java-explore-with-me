package ru.practicum.dto;

import lombok.Builder;
import lombok.Getter;
import ru.practicum.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class GetEventParametersDto {
    private List<Long> users;
    private List<Status> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
    private String text;
    private List<Long> categoryIds;
    private Boolean paid;
    private Boolean onlyAvailable;
    private String sort;
}
