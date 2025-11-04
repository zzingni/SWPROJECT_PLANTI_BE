package com.planti.domain.userplant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "companion_plants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPlant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "companion_plant_id")
    private Integer companionPlantId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "plant_id", nullable = false)
    private Integer plantId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Column(name = "watering_cycle")
    private Integer wateringCycle;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}