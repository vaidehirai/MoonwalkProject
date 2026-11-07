package com.vaidehi.moonwalk.controller;

import com.vaidehi.moonwalk.entity.Item;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/item")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService){
        this.itemService=itemService;
    }

    @GetMapping("/get-all-items")
    public ResponseEntity<APIResponse> getItems(){
        return itemService.getItems();
    }

    @GetMapping("/get-item-by-id/{itemId}")
    public ResponseEntity<APIResponse> getItemById(@PathVariable Long itemId){
        return itemService.getItemById(itemId);
    }

    @PostMapping("/insert-item")
    public ResponseEntity<APIResponse> insertItem(@RequestBody Item item){
        return itemService.insertItem(item);
    }

}
