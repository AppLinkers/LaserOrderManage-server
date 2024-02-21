package com.laser.ordermanage.ingredient.domain.type;

import com.laser.ordermanage.common.exception.CommonErrorCode;
import com.laser.ordermanage.common.exception.CustomCommonException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public enum IngredientStockType {
    ALL("all"),
    INCOMING("incoming"),
    PRODUCTION("production"),
    STOCK("stock"),
    OPTIMAL_STOCK("optimal-stock");

    @Getter
    private final String request;

    private static final Map<String, IngredientStockType> ingredientStockTypeMap =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(IngredientStockType::getRequest, Function.identity())));

    public static List<IngredientStockType> ofRequest(List<String> requestList) {
        return requestList.stream()
                .map(
                        request -> Optional.ofNullable(ingredientStockTypeMap.get(request))
                                .orElseThrow(() -> new CustomCommonException(CommonErrorCode.INVALID_PARAMETER, "stock-item 파라미터가 올바르지 않습니다.")))
                .collect(Collectors.toList());
    }
}
