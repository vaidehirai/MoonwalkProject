package com.vaidehi.moonwalk.controller;

import com.vaidehi.moonwalk.entity.Chef;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.service.ChefService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chefs")
public class ChefController {

    private final ChefService chefService;

    public ChefController(ChefService chefService) {
        this.chefService = chefService;
    }

    @PostMapping("/insert-chef")
    public ResponseEntity<APIResponse> insertChef(@RequestBody Chef chef){
        return chefService.insertChef(chef);
    }

    @DeleteMapping("/delete-chef/{chefId}")
    public ResponseEntity<APIResponse> deleteChef(@PathVariable Integer chefId) {
        return chefService.deleteChefById(chefId);
    }

    @GetMapping("/get-all-chefs")
    public ResponseEntity<APIResponse> getAllChefs() {
        return chefService.getAllChefs();
    }
}
