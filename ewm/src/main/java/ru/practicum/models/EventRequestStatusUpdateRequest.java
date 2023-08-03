package ru.practicum.models;

import lombok.*;
import ru.practicum.enums.Status;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    @Enumerated(EnumType.STRING)
    private Status status;
}
