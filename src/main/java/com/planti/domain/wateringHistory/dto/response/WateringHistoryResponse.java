package com.planti.domain.wateringHistory.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WateringHistoryResponse {
    private long wateringHistoryId;
    private long companionPlantId;
    private LocalDateTime wateringDate;
    private String wateringStatus;
}
