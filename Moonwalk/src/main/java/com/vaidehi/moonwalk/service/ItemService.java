package com.vaidehi.moonwalk.service;

import com.vaidehi.moonwalk.entity.Item;
import com.vaidehi.moonwalk.repository.ItemRepository;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.response.ItemResponse;
import com.vaidehi.moonwalk.response.ItemsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public ResponseEntity<APIResponse> getItems(){
        log.info("Fetching all items");
        List<Item> allItems= itemRepository.findAll();
        if(allItems.isEmpty()){
            log.warn("No items found in the repository");
            APIResponse apiResponse=new APIResponse();
            apiResponse.setMsg("No items found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
        else{
            ItemsResponse itemsResponse =new ItemsResponse(allItems);
            itemsResponse.setMsg("Items fetched successfully");
            log.info("Fetched {} items successfully", allItems.size());
            return ResponseEntity.status(HttpStatus.OK).body(itemsResponse);
        }
    }

    public ResponseEntity<APIResponse> getItemById(Long itemId){
        log.info("Fetching item by id {}", itemId);
        Integer itemid=Math.toIntExact(itemId);
        Optional<Item> item=itemRepository.findById(itemid);
        if(item.isPresent()){
            ItemResponse itemResponse =new ItemResponse(item.get());
            itemResponse.setMsg("Item fetched successfully");
            log.info("Item {} fetched successfully", itemId);
            return ResponseEntity.status(HttpStatus.OK).body(itemResponse);
        }
        else{
            log.warn("Item with id {} not found", itemId);
            APIResponse apiResponse=new APIResponse();
            apiResponse.setMsg("Item not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }

    }

    public ResponseEntity<APIResponse> insertItem(Item item){
        log.info("Inserting item {} with preparation time {}", item.getItemName(), item.getPreparationTime());
        itemRepository.save(item);
        APIResponse apiResponse=new APIResponse();
        apiResponse.setMsg("Item inserted successfully");
        log.info("Item {} inserted successfully", item.getItemName());
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
