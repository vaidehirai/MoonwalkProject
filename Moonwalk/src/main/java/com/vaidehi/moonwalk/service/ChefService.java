package com.vaidehi.moonwalk.service;

import com.vaidehi.moonwalk.entity.Chef;
import com.vaidehi.moonwalk.repository.ChefRepository;
import com.vaidehi.moonwalk.response.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;

@Service
public class ChefService {

    private static final Logger log = LoggerFactory.getLogger(ChefService.class);

    private final ChefRepository chefRepository;

    public ChefService(ChefRepository chefRepository) {
        this.chefRepository = chefRepository;
    }

    public ResponseEntity<APIResponse> insertChef(Chef chef){
        log.info("Inserting chef {}", chef.getChefName());
        chefRepository.save(chef);
        OrderService.chefQueues.add(new ArrayDeque<>());
        log.info("Chef {} inserted and queue initialized successfully", chef.getChefName());
        return ResponseEntity.status(HttpStatus.OK).body(new APIResponse("Chef inserted successfully"));
    }

}
