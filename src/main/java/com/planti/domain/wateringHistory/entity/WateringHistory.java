package com.planti.domain.wateringHistory.entity;

import com.planti.domain.userplant.entity.UserPlant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="watering_history")
@NoArgsConstructor
@AllArgsConstructor
public class WateringHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="watering_history_id")
    private long wateringHistoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="companion_plant_id", nullable = false)
    private UserPlant companionPlant;

    @Column(name="watering_date")
    private LocalDateTime  wateringDate;

    @Column(name="watering_status")
    private String wateringStatus;
}
