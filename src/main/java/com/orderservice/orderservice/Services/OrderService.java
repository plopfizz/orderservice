package com.orderservice.orderservice.Services;

import com.orderservice.orderservice.DTO.RequestOrderDTO;
import com.orderservice.orderservice.Entities.Order;

public interface OrderService {


    Order createOrder(RequestOrderDTO requestOrderDTO);
}
