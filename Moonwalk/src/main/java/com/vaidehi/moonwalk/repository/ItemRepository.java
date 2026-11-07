package com.vaidehi.moonwalk.repository;

import com.vaidehi.moonwalk.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ItemRepository extends JpaRepository<Item, Integer> {
}
