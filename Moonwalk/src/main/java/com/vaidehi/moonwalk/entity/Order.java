package com.vaidehi.moonwalk.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@IdClass(OrderId.class)
@Table(name="customerOrder")
public class Order {

    //Created  a composite primary key of orderId and itemId so that 2 orders with same items do not get rejected to enter into the database.
    @Id
    private Long orderId;
    @Id
    private Long itemId;
    private LocalTime timeWhenOrderReady;

}
