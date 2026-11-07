package com.vaidehi.moonwalk.service;

import com.vaidehi.moonwalk.entity.Item;
import com.vaidehi.moonwalk.repository.ItemRepository;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.response.ItemResponse;
import com.vaidehi.moonwalk.response.ItemsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
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
        try {
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
        } catch (DataAccessException exception) {
            log.error("Failed to fetch items", exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse("Failed to fetch items"));
        }
    }

    public ResponseEntity<APIResponse> getItemById(Long itemId){
        try {
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
        } catch (ArithmeticException exception) {
            log.warn("Item id {} is outside the supported range", itemId, exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new APIResponse("Invalid item id"));
        } catch (DataAccessException exception) {
            log.error("Failed to fetch item {}", itemId, exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse("Failed to fetch item"));
        }
    }

    public ResponseEntity<APIResponse> insertItem(Item item){
        try {
            log.info("Inserting item {} with preparation time {}", item.getItemName(), item.getPreparationTime());
            itemRepository.save(item);
            APIResponse apiResponse=new APIResponse();
            apiResponse.setMsg("Item inserted successfully");
            log.info("Item {} inserted successfully", item.getItemName());
            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (DataAccessException exception) {
            log.error("Failed to insert item {}", item.getItemName(), exception);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new APIResponse("Failed to insert item"));
        }
    }
}
