package com.orderservice.orderservice.Repositories;

import com.orderservice.orderservice.Entities.OutBoxOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutBoxOrderRepository  extends JpaRepository<OutBoxOrderEntity,Long> {
}
