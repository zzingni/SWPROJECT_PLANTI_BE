package com.planti.domain.wateringHistory.controller;
import com.planti.domain.wateringHistory.dto.request.WateringHistoryCreateRequest;
import com.planti.domain.wateringHistory.dto.response.WateringHistoryResponse;
import com.planti.domain.wateringHistory.service.WateringHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watering")
public class WateringHistoryController {
    private final WateringHistoryService wateringHistoryService;

    public WateringHistoryController(WateringHistoryService wateringHistoryService) {
        this.wateringHistoryService = wateringHistoryService;
    }

    @PostMapping("/history")
    @ResponseStatus(HttpStatus.CREATED)
    public WateringHistoryResponse saveWatering(@Valid @RequestBody WateringHistoryCreateRequest req) {
        return wateringHistoryService.create(req);
    }

    @GetMapping("/history")
    public List<WateringHistoryResponse> getWateringHistory(@RequestParam Long companionPlantId) {
        return wateringHistoryService.getHistory(companionPlantId);
    }
}
