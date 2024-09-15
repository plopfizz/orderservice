//package com.orderservice.orderservice.Configs;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.orderservice.orderservice.Entities.OutBoxOrderEntity;
//import com.orderservice.orderservice.Repositories.OutBoxOrderRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class OutBoxProcessor {
//
//    @Autowired
//    private OutBoxOrderRepository outboxRepository;
//
//    @Autowired
//    private KafkaTemplate<String, Object> kafkaTemplate;
//
//    @Transactional
//    public void processOutboxEvent(OutBoxOrderEntity event) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode jsonNode = objectMapper.valueToTree(event);
//
//            kafkaTemplate.send("update_order_quantity", jsonNode);
//
//            System.out.println("Order has been reserved and event published to Kafka.");
//
//            // After successful Kafka publish, delete the outbox event
//            outboxRepository.delete(event);
//        } catch (Exception e) {
//            e.printStackTrace(); // Handle exception
//            // Transaction will be rolled back if there is any failure
//        }
//    }
//
//
//    @Scheduled(cron = "0 * * * * *")
//    public void processPendingOutboxEvents() {
//        List<OutBoxOrderEntity> pendingEvents = outboxRepository.findAll();
//
//        for (OutBoxOrderEntity event : pendingEvents) {
//            processOutboxEvent(event);
//        }
//    }
//}
