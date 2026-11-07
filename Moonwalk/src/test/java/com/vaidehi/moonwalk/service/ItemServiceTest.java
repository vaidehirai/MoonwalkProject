package com.vaidehi.moonwalk.service;

import com.vaidehi.moonwalk.entity.Item;
import com.vaidehi.moonwalk.repository.ItemRepository;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.response.ItemResponse;
import com.vaidehi.moonwalk.response.ItemsResponse;
import org.springframework.dao.DataAccessResourceFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItems_WhenNoItemsExist_ReturnsNotFound() {
        when(itemRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<APIResponse> response = itemService.getItems();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No items found", response.getBody().getMsg());
    }

    @Test
    void getItems_WhenItemsExist_ReturnsOkWithItems() {
        List<Item> items = List.of(
                new Item(1L, "Burger", 50, 10),
                new Item(2L, "Pizza", 120, 15)
        );
        when(itemRepository.findAll()).thenReturn(items);

        ResponseEntity<APIResponse> response = itemService.getItems();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Items fetched successfully", response.getBody().getMsg());
        assertInstanceOf(ItemsResponse.class, response.getBody());
        assertEquals(2, ((ItemsResponse) response.getBody()).getItems().size());
    }

    @Test
    void getItems_WhenRepositoryFails_ReturnsInternalServerError() {
        when(itemRepository.findAll()).thenThrow(new DataAccessResourceFailureException("database unavailable"));

        ResponseEntity<APIResponse> response = itemService.getItems();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to fetch items", response.getBody().getMsg());
    }

    @Test
    void getItemById_WhenItemExists_ReturnsItem() {
        Item item = new Item(1L, "Burger", 50, 10);
        when(itemRepository.findById(1)).thenReturn(Optional.of(item));

        ResponseEntity<APIResponse> response = itemService.getItemById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Item fetched successfully", response.getBody().getMsg());
        assertInstanceOf(ItemResponse.class, response.getBody());
        assertEquals(item, ((ItemResponse) response.getBody()).getItem());
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ReturnsNotFound() {
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<APIResponse> response = itemService.getItemById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Item not found", response.getBody().getMsg());
    }

    @Test
    void getItemById_WhenRepositoryFails_ReturnsInternalServerError() {
        when(itemRepository.findById(1))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        ResponseEntity<APIResponse> response = itemService.getItemById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to fetch item", response.getBody().getMsg());
    }

    @Test
    void insertItem_WhenCalled_SavesItemAndReturnsCreated() {
        Item item = new Item(1L, "Burger", 50, 10);

        ResponseEntity<APIResponse> response = itemService.insertItem(item);

        verify(itemRepository).save(item);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Item inserted successfully", response.getBody().getMsg());
    }

    @Test
    void insertItem_WhenRepositoryFails_ReturnsInternalServerError() {
        Item item = new Item(null, "Burger", 50, 10);
        doThrow(new DataAccessResourceFailureException("database unavailable"))
                .when(itemRepository).save(item);

        ResponseEntity<APIResponse> response = itemService.insertItem(item);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to insert item", response.getBody().getMsg());
    }
}
