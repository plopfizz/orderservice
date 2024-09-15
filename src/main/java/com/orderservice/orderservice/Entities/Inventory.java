package com.orderservice.orderservice.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data

@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    private Long id;

    private String productId;
    private Integer quantity;
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
