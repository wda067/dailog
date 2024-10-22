package com.dailog.api.controller;

import com.dailog.api.response.stock.StockDetail;
import com.dailog.api.response.stock.StockInfo;
import com.dailog.api.response.stock.StockChart;
import com.dailog.api.response.stock.StockSearch;
import com.dailog.api.service.StockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/api/stock/token")
    public void getAccessToken() {
        stockService.getAccessToken();
    }

    @GetMapping("/api/stock/{ticker}")
    public StockDetail getStockDetail(@PathVariable("ticker") String ticker) {
        return stockService.getStockDetail(ticker);
    }

    @GetMapping("/api/stock/{ticker}/info")
    public StockInfo getStockInfo(@PathVariable("ticker") String ticker) {
        return stockService.getStockInfo(ticker);
    }

    @GetMapping("/api/stock/{ticker}/price/history")
    public List<StockChart> getStockPriceHistory(@PathVariable("ticker") String ticker,
                                                 @RequestParam String dateType,
                                                 @RequestParam(defaultValue = "") String searchDate) {
        return stockService.getStockHistory(ticker, dateType, searchDate);
    }

    @GetMapping("/api/stock/{ticker}/price/minutes")
    public List<StockChart> getStockPriceByMinutes(@PathVariable("ticker") String ticker,
                                                   @RequestParam(defaultValue = "5") String intervalMinutes) {
        return stockService.getStockPriceByMinutes(ticker, intervalMinutes);
    }

    @GetMapping("/api/stock/search")
    public List<StockSearch> searchStock(@RequestParam String query) {
        return stockService.searchTickers(query);
    }
}
