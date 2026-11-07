package com.vaidehi.moonwalk.response;

import com.vaidehi.moonwalk.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ItemResponse extends APIResponse{

    private Item item;
}
