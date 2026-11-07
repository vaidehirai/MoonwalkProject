package com.vaidehi.moonwalk.response;

import com.vaidehi.moonwalk.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderResponse extends APIResponse{

    private List<Order> allOrders;
}
