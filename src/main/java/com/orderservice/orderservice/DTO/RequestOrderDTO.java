package com.orderservice.orderservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestOrderDTO {
    private String productId;
    private Integer quantity;
    private Long userId;
    private Double price;

}

