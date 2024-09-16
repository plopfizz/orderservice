package com.orderservice.orderservice.DTO;

import com.orderservice.orderservice.Entities.PaymentStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long orderId;
    private PaymentStatusEnum status;
}
