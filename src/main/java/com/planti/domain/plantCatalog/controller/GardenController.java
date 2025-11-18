package com.planti.domain.plantCatalog.controller;

import com.planti.domain.plantCatalog.dto.PlantDto;
import com.planti.domain.plantCatalog.service.GardenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/garden")
@RequiredArgsConstructor
public class GardenController {

    private final GardenService service;

    @GetMapping("/search")
    public ResponseEntity<List<PlantDto>> searchByName(
            @RequestParam("name") String name,
            @RequestParam(value = "pageNo", defaultValue = "1") String pageNo,
            @RequestParam(value = "numOfRows", defaultValue = "10") String numOfRows
    ) {

        Map<String, String> params = new HashMap<>();
        params.put("sType", "cntntsSj");   // 제목(식물명) 검색
        params.put("sText", name.trim());
        params.put("pageNo", pageNo);
        params.put("numOfRows", numOfRows);

        List<PlantDto> result = service.getGardenList(params);
        return ResponseEntity.ok(result);
    }
}