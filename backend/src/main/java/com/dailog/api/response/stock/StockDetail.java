package com.dailog.api.response.stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockDetail {

    private String last;
    private String diff;
    private String rate;
    private String marketCap;
    private String high52Weeks;
    private String high52WeeksDate;
    private String low52Weeks;
    private String low52WeeksDate;
    private String per;
    private String pbr;
    private String eps;
    private String bps;
    private String totalShares;
    private String sector;
}
