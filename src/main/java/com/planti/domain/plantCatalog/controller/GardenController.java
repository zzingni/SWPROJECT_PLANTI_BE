package com.planti.domain.plantCatalog.controller;

import com.planti.domain.plantCatalog.dto.PlantDto;
import com.planti.domain.plantCatalog.dto.PlantNameDto;
import com.planti.domain.plantCatalog.service.GardenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/garden")
@RequiredArgsConstructor
public class GardenController {

    private final GardenService service;

    @PostMapping("/search")
    public ResponseEntity<List<PlantDto>> searchByNamePost(
            @Valid @RequestBody PlantNameDto dto,
            @RequestParam(value = "pageNo", defaultValue = "1") String pageNo,
            @RequestParam(value = "numOfRows", defaultValue = "10") String numOfRows
    ) {
        String name = dto.getPlantName() == null ? "" : dto.getPlantName().trim();

        Map<String, String> params = new HashMap<>();
        params.put("sType", "cntntsSj");
        params.put("sText", name);
        params.put("pageNo", pageNo);
        params.put("numOfRows", numOfRows);

        List<PlantDto> result = service.getGardenList(params);
        return ResponseEntity.ok(result);
    }
}