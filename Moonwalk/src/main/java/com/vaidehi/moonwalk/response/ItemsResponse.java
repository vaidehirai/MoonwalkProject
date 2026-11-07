package com.vaidehi.moonwalk.response;

import com.vaidehi.moonwalk.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ItemsResponse extends APIResponse{

    private List<Item> items;
}
