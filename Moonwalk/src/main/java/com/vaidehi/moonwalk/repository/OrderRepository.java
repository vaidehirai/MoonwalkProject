package com.vaidehi.moonwalk.repository;

import com.vaidehi.moonwalk.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
