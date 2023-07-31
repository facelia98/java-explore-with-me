package ru.practicum.models;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<Long> confirmedRequests = new ArrayList<>();
    private List<Long> rejectedRequests = new ArrayList<>();
}
