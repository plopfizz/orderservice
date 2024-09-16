package com.orderservice.orderservice.Repositories;

import com.orderservice.orderservice.Entities.Order;
import com.orderservice.orderservice.Entities.OrderStatusEnum;
import com.orderservice.orderservice.Entities.PaymentStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByStatus(OrderStatusEnum orderStatusEnum);

    List<Order> findByPaymentStatus(PaymentStatusEnum paymentStatusEnum);
}
