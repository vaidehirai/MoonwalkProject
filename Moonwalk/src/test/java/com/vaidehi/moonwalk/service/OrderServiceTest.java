package com.vaidehi.moonwalk.service;

import com.vaidehi.moonwalk.entity.Item;
import com.vaidehi.moonwalk.entity.Order;
import com.vaidehi.moonwalk.repository.OrderRepository;
import com.vaidehi.moonwalk.request.OrderRequest;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.response.ItemResponse;
import com.vaidehi.moonwalk.response.OrderResponse;
import org.springframework.dao.DataAccessResourceFailureException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        OrderService.chefQueues = new ArrayList<>();
        OrderService.chefQueues.add(new ArrayDeque<>());
    }

    @AfterEach
    void tearDown() {
        OrderService.chefQueues = new ArrayList<>();
    }

    @Test
    void createOrder_WhenItemsAreProvided_SavesEachOrderAndAssignsChefs() {
        OrderRequest request = new OrderRequest(101L, new ArrayList<>(List.of(1L, 2L)));
        Item burger = new Item(1L, "Burger", 50, 5);
        Item pizza = new Item(2L, "Pizza", 120, 10);

        when(itemService.getItemById(1L)).thenReturn(ResponseEntity.ok(createItemResponse(burger)));
        when(itemService.getItemById(2L)).thenReturn(ResponseEntity.ok(createItemResponse(pizza)));

        ResponseEntity<APIResponse> response = orderService.createOrder(request);

        verify(orderRepository, times(2)).save(any(Order.class));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order created successfully", response.getBody().getMsg());
        assertEquals(2, OrderService.chefQueues.get(0).size());
    }

    @Test
    void createOrder_WhenRepositoryFails_ReturnsInternalServerError() {
        OrderRequest request = new OrderRequest(101L, new ArrayList<>(List.of(1L)));
        Item burger = new Item(1L, "Burger", 50, 5);
        when(itemService.getItemById(1L)).thenReturn(ResponseEntity.ok(createItemResponse(burger)));
        doThrow(new DataAccessResourceFailureException("database unavailable"))
                .when(orderRepository).save(any(Order.class));

        ResponseEntity<APIResponse> response = orderService.createOrder(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to create order", response.getBody().getMsg());
    }

    @Test
    void getActiveOrders_WhenNoActiveOrdersExist_ReturnsNotFound() {
        Order expiredOrder = new Order(1L, 1L, LocalTime.now().minusMinutes(5));
        when(orderRepository.findAll()).thenReturn(List.of(expiredOrder));

        ResponseEntity<APIResponse> response = orderService.getActiveOrders();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No active orders", response.getBody().getMsg());
    }

    @Test
    void getActiveOrders_WhenActiveOrdersExist_ReturnsOrders() {
        Order activeOrder = new Order(1L, 1L, LocalTime.now().plusMinutes(5));
        when(orderRepository.findAll()).thenReturn(List.of(activeOrder));

        ResponseEntity<APIResponse> response = orderService.getActiveOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Active orders fetched successfully", response.getBody().getMsg());
        assertInstanceOf(OrderResponse.class, response.getBody());
        assertEquals(1, ((OrderResponse) response.getBody()).getAllOrders().size());
    }

    @Test
    void getActiveOrders_WhenRepositoryFails_ReturnsInternalServerError() {
        when(orderRepository.findAll())
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        ResponseEntity<APIResponse> response = orderService.getActiveOrders();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to fetch active orders", response.getBody().getMsg());
    }

    private ItemResponse createItemResponse(Item item) {
        ItemResponse itemResponse = new ItemResponse();
        itemResponse.setItem(item);
        itemResponse.setMsg("Item fetched successfully");
        return itemResponse;
    }
}
