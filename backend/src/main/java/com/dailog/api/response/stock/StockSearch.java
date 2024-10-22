package com.dailog.api.response.stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockSearch {

    private String ticker;
    private String exchange;
    private String name;
}
