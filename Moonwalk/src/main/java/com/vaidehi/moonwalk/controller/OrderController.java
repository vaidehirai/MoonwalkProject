package com.vaidehi.moonwalk.controller;

import com.vaidehi.moonwalk.entity.Order;
import com.vaidehi.moonwalk.request.OrderRequest;
import com.vaidehi.moonwalk.response.APIResponse;
import com.vaidehi.moonwalk.response.OrderResponse;
import com.vaidehi.moonwalk.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<APIResponse> createOrder(@RequestBody OrderRequest orderRequest){
        return orderService.createOrder(orderRequest);
    }

    @GetMapping("/get-active-orders")
    public ResponseEntity<APIResponse> getActiveOrders(){
        return orderService.getActiveOrders();
    }
}
