package com.orderservice.orderservice.Services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.orderservice.DTO.PaymentResponse;
import com.orderservice.orderservice.DTO.RequestOrderDTO;
import com.orderservice.orderservice.Entities.*;
import com.orderservice.orderservice.Repositories.OrderRepository;
import com.orderservice.orderservice.Repositories.OutBoxOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderServiceImplementation implements OrderService{

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OutBoxOrderRepository outBoxOrderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired



    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Override
    @Transactional
    public Order createOrder(RequestOrderDTO requestOrderDTO) {


        Order order = new Order();
        order.setProductId(requestOrderDTO.getProductId());
        order.setQuantity(requestOrderDTO.getQuantity());
        order.setUserId(requestOrderDTO.getUserId());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setPaymentStatus(PaymentStatusEnum.PENDING);
        order.setTotalPrice(requestOrderDTO.getPrice());
        order.setOrderDate(LocalDateTime.now());
        Order createdOrder = orderRepository.save(order);

        OutBoxOrderEntity outBoxOrder = new OutBoxOrderEntity();
        outBoxOrder.setProductId(requestOrderDTO.getProductId());
        outBoxOrder.setOrderId(createdOrder.getId());
        outBoxOrder.setQuantity(requestOrderDTO.getQuantity());
        outBoxOrder.setStockAdjustmentEnum(StockAdjustmentEnum.DECREASE);
        outBoxOrder.setPrice(order.getTotalPrice());
        outBoxOrderRepository.save(outBoxOrder);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(outBoxOrder);
        kafkaTemplate.send("reserve_inventory", jsonNode);
        return createdOrder;
    }

    @KafkaListener(topics = "inventory_reserved")
    @Transactional
    public void handleInventoryReserved(JsonNode outBoxOrder) {
        // Inventory has been successfully reserved, now proceed to payment
        System.out.println("we are here in the inventory reserved kafka event"+outBoxOrder +" "+LocalDateTime.now());
        kafkaTemplate.send("process_payment", outBoxOrder);
    }

    @KafkaListener(topics = "inventory_not_available")
    @Transactional
    public void handleInventoryNotAvailable(JsonNode outBoxOrderJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        OutBoxOrderEntity outBoxOrder = objectMapper.treeToValue(outBoxOrderJson,OutBoxOrderEntity.class);
        // Inventory is not available, mark the order as failed
        try {
            Order order = orderRepository.findById(outBoxOrder.getOrderId()).orElseThrow();
            order.setStatus(OrderStatusEnum.FAILED);
            orderRepository.save(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @KafkaListener(topics = "payment_status")
    @Transactional
    public void handlePaymentStatus(JsonNode jsonNode) throws JsonProcessingException {
        // Mark the order as COMPLETED
        ObjectMapper objectMapper = new ObjectMapper();
        PaymentResponse paymentResponse = objectMapper.treeToValue(jsonNode, PaymentResponse.class);
        Order order = orderRepository.findById(paymentResponse.getOrderId()).orElseThrow();
        PaymentStatusEnum paymentStatus = (paymentResponse.getStatus());
        if(paymentStatus == PaymentStatusEnum.SUCCESS) {
            order.setPaymentStatus(paymentStatus);
            orderRepository.save(order);

        }
        else{
            order.setStatus(OrderStatusEnum.FAILED);
            order.setPaymentStatus(paymentStatus);
            orderRepository.save(order);
            OutBoxOrderEntity outBoxOrder = new OutBoxOrderEntity();

            outBoxOrder.setProductId(order.getProductId());
            outBoxOrder.setOrderId(order.getId());
            outBoxOrder.setQuantity(order.getQuantity());
            outBoxOrder.setStockAdjustmentEnum(StockAdjustmentEnum.INCREASE );
            outBoxOrder.setPrice(order.getTotalPrice());
            JsonNode node = objectMapper.valueToTree(outBoxOrder);
            // Send event to release inventory
            System.out.println("we are in the order failed " +" "+ LocalDateTime.now());
            kafkaTemplate.send("release_inventory", node);
        }
    }


}
