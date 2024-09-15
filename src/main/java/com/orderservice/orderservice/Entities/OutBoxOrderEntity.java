package com.orderservice.orderservice.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

 @Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OutBoxOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;
    private String productId;
    private Long orderId;
    private StockAdjustmentEnum stockAdjustmentEnum;

}
