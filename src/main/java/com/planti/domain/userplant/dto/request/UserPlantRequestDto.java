package com.planti.domain.userplant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlantRequestDto {

    @NotNull
    private long plantId;       // plant_id

    @NotNull
    private String plantNickName;      // 반려식물 닉네임

    @NotNull
    private String wateringCycle; // 물 주기 주기
}