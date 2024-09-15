
# Order Service

## Overview

The `OrderService` package is responsible for handling order-related operations within the `orderservice` microservice. It includes functionality for creating orders, handling inventory reservations, and processing payment results. 

## Classes

### `OrderServiceImplementation`

This class implements the `OrderService` interface and provides methods for order management. It uses Kafka for asynchronous communication and handles various order-related events.

#### Dependencies

- `OrderRepository` - Repository for accessing and managing orders.
- `OutBoxOrderRepository` - Repository for handling outbox order entities.
- `KafkaTemplate` - Kafka template for sending messages to Kafka topics.
- `ObjectMapper` - Jackson object mapper for converting between Java objects and JSON.

#### Methods

- **`createOrder(RequestOrderDTO requestOrderDTO)`**: 
  - Creates a new order based on the provided `RequestOrderDTO`.
  - Saves the order to the database.
  - Creates an `OutBoxOrderEntity` for inventory reservation and sends it to the `reserve_inventory` Kafka topic.

- **`handleInventoryReserved(JsonNode outBoxOrder)`**:
  - Listens to the `inventory_reserved` Kafka topic.
  - Proceeds to payment processing if inventory is successfully reserved.

- **`handleInventoryNotAvailable(JsonNode outBoxOrderJson)`**:
  - Listens to the `inventory_not_available` Kafka topic.
  - Marks the order as failed if inventory is not available.

- **`handlePaymentSuccess(JsonNode jsonNode)`**:
  - Listens to the `payment_success` Kafka topic.
  - Marks the order as completed upon successful payment.

- **`handlePaymentFailed(JsonNode jsonNode)`**:
  - Listens to the `payment_failed` Kafka topic.
  - Marks the order as failed and sends an event to release inventory if payment fails.

### `PaymentConfirmationSchedulingService`

This service is responsible for processing pending orders at scheduled intervals. It refills inventory for orders that are still in the `PENDING` state and sends a Kafka message to update the order quantity.

#### Dependencies

- `OrderRepository` - Repository for accessing and managing orders.
- `KafkaTemplate` - Kafka template for sending messages to Kafka topics.
- `ObjectMapper` - Jackson object mapper for converting between Java objects and JSON.

#### Methods

- **`processPendingOrders()`**:
  - Scheduled to run every 2 minutes (cron expression: `0 0/2 * * * ?`).
  - Fetches all orders with the status `PENDING`.
  - Sends a message to the `update_order_quantity` Kafka topic to refill inventory.
  - Deletes the processed orders from the database.



### Scheduling

- The `PaymentConfirmationSchedulingService` uses Spring's `@Scheduled` annotation to periodically process pending orders. Adjust the cron expression according to your requirements.

## Error Handling

- All Kafka listeners include basic exception handling. Ensure you have proper error logging and handling mechanisms in place in a production environment.

## Notes

- Ensure the `OutBoxOrderEntity` and `Order` entities are properly configured and mapped to your database schema.
- Kafka topics used: `reserve_inventory`, `inventory_reserved`, `inventory_not_available`, `process_payment`, `payment_success`, `payment_failed`, `update_order_quantity`.

