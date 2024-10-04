package com.skillbox.cryptobot.model;

import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("subscribes")
public class Subscribe {

    @Id
    @GeneratedValue
    private UUID id;
    private Long userId;
    private Integer desiredPrice;
    private Long lastInteractionTime;
}