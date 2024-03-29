package com.laser.ordermanage.ingredient.dto.response;

import lombok.Builder;

@Builder
public record GetIngredientStockDetailResponse (
        Number previousDay,
        Number incoming,
        Number production,
        Number currentDay,
        Number optimal
) { }
