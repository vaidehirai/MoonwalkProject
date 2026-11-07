package com.vaidehi.moonwalk.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderId implements Serializable{

    private Long orderId;
    private Long itemId;
}
