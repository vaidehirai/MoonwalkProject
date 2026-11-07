package com.vaidehi.moonwalk.repository;

import com.vaidehi.moonwalk.entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChefRepository extends JpaRepository<Chef, Integer> {
}
