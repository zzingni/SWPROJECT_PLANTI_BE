package com.planti.domain.notification.entity;

import com.planti.domain.user.entity.User;
import com.planti.domain.userplant.entity.UserPlant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "companion_plant_id", nullable = false)
    private UserPlant userPlant;

    @Column(nullable = false)
    private String type; // WATER, ENV, SYSTEM

    @Column(name = "last_sent", nullable = false)
    private LocalDateTime lastSent;
}