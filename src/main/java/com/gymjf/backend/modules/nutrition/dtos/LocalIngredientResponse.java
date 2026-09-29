package com.gymjf.backend.modules.nutrition.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocalIngredientResponse {

    private Integer id;
    private String name;
    private String imageUrl;

}
