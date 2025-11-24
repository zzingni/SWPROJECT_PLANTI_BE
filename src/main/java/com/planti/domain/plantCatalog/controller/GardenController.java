package com.planti.domain.plantCatalog.controller;

import com.planti.domain.plantCatalog.dto.PlantDto;
import com.planti.domain.plantCatalog.dto.PlantNameDto;
import com.planti.domain.plantCatalog.service.GardenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/garden")
@RequiredArgsConstructor
public class GardenController {

    private final GardenService service;

    @PostMapping("/search")
    public ResponseEntity<List<PlantDto>> searchByNamePost(
            @Valid @RequestBody PlantNameDto dto,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "numOfRows", defaultValue = "10") int numOfRows
    ) {
        // 서비스 메서드에 DTO 그대로 전달
        List<PlantDto> result = service.searchByName(dto, pageNo, numOfRows);
        return ResponseEntity.ok(result);
    }
}