package com.orderservice.orderservice.Controllers;


import com.orderservice.orderservice.DTO.RequestOrderDTO;
import com.orderservice.orderservice.Entities.Order;
import com.orderservice.orderservice.Services.OrderService;
import com.orderservice.orderservice.Services.OutBoxOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;


    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody RequestOrderDTO requestOrderDTO) {
        Order order = orderService.createOrder(requestOrderDTO);
        return ResponseEntity.ok(order);
    }

}
