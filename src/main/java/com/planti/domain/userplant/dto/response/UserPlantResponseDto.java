package com.planti.domain.userplant.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPlantResponseDto {
    private Long companionPlantId;
    private Long plantId;
    private String nickname;
    private String wateringCycle;
}
