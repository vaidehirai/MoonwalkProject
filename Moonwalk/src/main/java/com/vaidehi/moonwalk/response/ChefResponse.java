package com.vaidehi.moonwalk.response;

import com.vaidehi.moonwalk.entity.Chef;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ChefResponse extends APIResponse{

    private List<Chef> chefs;
}
