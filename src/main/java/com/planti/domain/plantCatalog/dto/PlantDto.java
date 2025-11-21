package com.planti.domain.plantCatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantDto {
    private String id;
    private String name;            // 식물명
    private String scientificName;  // 식물학명
    private String family;          // 과목
    private String watering;        // 물주기
    private String temperature;     // 적정 온도
    private String humidity;        // 적정 습도
    private String pestControl;     // 병충해 관리
    private String functionality;   // 기능성 정보
    private String specialCare;     // 특별관리 정보
    private String toxicity;        // 독성 정보
}