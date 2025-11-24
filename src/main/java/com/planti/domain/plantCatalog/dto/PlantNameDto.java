package com.planti.domain.plantCatalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantNameDto {
    @NotBlank(message = "식물 이름은 필수입니다.")
    private String plantName;
}
