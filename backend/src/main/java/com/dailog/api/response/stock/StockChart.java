package com.dailog.api.response.stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockChart {

    private String date;    //일자(YYYYMMDD)
    private String open;    //시가
    private String close;   //종가
    private String high;    //고가
    private String low;     //저가
    private String diff;    //전일과 종가 차이
    private String rate;    //등락율
    private String volume;  //거래량
}
