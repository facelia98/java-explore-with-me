package ru.practicum.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequest> confirmedRequests;
    private List<ParticipationRequest> rejectedRequests;
}
