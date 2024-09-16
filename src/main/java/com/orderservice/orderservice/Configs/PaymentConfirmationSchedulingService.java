package com.orderservice.orderservice.Configs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderservice.orderservice.Entities.*;
import com.orderservice.orderservice.Repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentConfirmationSchedulingService {
 @Autowired
    private OrderRepository orderRepository;  // To access orders
    @Autowired
    public KafkaTemplate<String, Object> kafkaTemplate;
//    @Scheduled(cron = "0 0/15 * * * ?")// 900000 ms = 15 minutes
@Scheduled(cron = "0 0/2 * * * ?")
@Transactional
    public void processPendingOrders() {
        // Fetch all orders with status "PENDING"
        List<Order> pendingOrders = orderRepository.findByPaymentStatus(PaymentStatusEnum.PENDING);
        ObjectMapper objectMapper = new ObjectMapper();
        for (Order order : pendingOrders) {
            // Refill inventory for the product in the order
                try{

                    OutBoxOrderEntity outBoxOrder = new OutBoxOrderEntity();
                    outBoxOrder.setProductId(order.getProductId());
                    outBoxOrder.setOrderId(order.getId());
                    outBoxOrder.setQuantity(order.getQuantity());
                    outBoxOrder.setStockAdjustmentEnum(StockAdjustmentEnum.INCREASE );
                    outBoxOrder.setPrice(order.getTotalPrice());
                    JsonNode jsonNode = objectMapper.valueToTree(outBoxOrder);
                    kafkaTemplate.send("update_order_quantity", jsonNode);
                    orderRepository.delete(order);
                    System.out.println("Processed pending order: " + order.getId() +" "+ LocalDateTime.now());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


        }
    }

}
