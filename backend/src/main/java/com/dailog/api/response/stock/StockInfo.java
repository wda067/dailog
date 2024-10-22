package com.dailog.api.response.stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockInfo {

    private String country;
    private String market;
    private String name;
}
