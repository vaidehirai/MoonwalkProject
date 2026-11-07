package com.vaidehi.moonwalk.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor

//For accepting multiple items in a single order
public class OrderRequest {

    private Long orderId;
    private ArrayList<Long> itemIds;
}
