package com.orderservice.orderservice.Services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        order.setTotalPrice(requestOrderDTO.getPrice());
        order.setOrderDate(LocalDateTime.now());
        Order createdOrder = orderRepository.save(order);

        OutBoxOrderEntity outBoxOrder = new OutBoxOrderEntity();
        outBoxOrder.setProductId(requestOrderDTO.getProductId());
        outBoxOrder.setOrderId(createdOrder.getId());
        outBoxOrder.setQuantity(requestOrderDTO.getQuantity());
        outBoxOrder.setStockAdjustmentEnum(StockAdjustmentEnum.DECREASE);
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
        System.out.println("we are here in the inventory reserved kafka event"+outBoxOrder);
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
    @KafkaListener(topics = "payment_success")
    @Transactional
    public void handlePaymentSuccess(JsonNode jsonNode) throws JsonProcessingException {
        // Mark the order as COMPLETED
        ObjectMapper objectMapper = new ObjectMapper();
        OutBoxOrderEntity outBoxOrder = objectMapper.treeToValue(jsonNode, OutBoxOrderEntity.class);
        Order order = orderRepository.findById(outBoxOrder.getOrderId()).orElseThrow();
        order.setStatus(OrderStatusEnum.COMPLETE);
        orderRepository.save(order);
    }
    @KafkaListener(topics = "payment_failed")
    @Transactional
    public void handlePaymentFailed(JsonNode jsonNode) throws JsonProcessingException {
        // Mark the order as FAILED and release reserved inventory
        ObjectMapper objectMapper = new ObjectMapper();
        OutBoxOrderEntity outBoxOrder = objectMapper.treeToValue(jsonNode, OutBoxOrderEntity.class);
        Order order = orderRepository.findById(outBoxOrder.getOrderId()).orElseThrow();
        order.setStatus(OrderStatusEnum.FAILED);
        orderRepository.save(order);
        JsonNode node = objectMapper.valueToTree(outBoxOrder);
        // Send event to release inventory
        kafkaTemplate.send("release_inventory", node);
    }

}
