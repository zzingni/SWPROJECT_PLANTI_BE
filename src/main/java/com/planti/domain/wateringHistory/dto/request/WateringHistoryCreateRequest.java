package com.planti.domain.wateringHistory.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WateringHistoryCreateRequest {
    private long companionPlantId;
    private String wateringStatus;
}
