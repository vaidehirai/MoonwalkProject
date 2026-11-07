package com.vaidehi.moonwalk.controller;

import com.vaidehi.moonwalk.entity.Chef;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.service.ChefService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chefs")
public class ChefController {

    private final ChefService chefService;

    public ChefController(ChefService chefService) {
        this.chefService = chefService;
    }

    @PostMapping("/insert-chef")
    public ResponseEntity<APIResponse> insertChef(Chef chef){
        return chefService.insertChef(chef);
    }
}
