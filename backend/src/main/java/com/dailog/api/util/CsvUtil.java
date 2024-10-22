package com.dailog.api.util;

import com.dailog.api.exception.InvalidRequest;
import com.dailog.api.exception.stock.StockNotFound;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CsvUtil {

    private static final Map<String, List<String>> stockExchangeMap = new HashMap<>();

    public CsvUtil() {
        loadExchangeCodes();
    }

    //CSV 파일을 로드해서 Map에 저장
    private static void loadExchangeCodes() {
        ClassLoader classLoader = CsvUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("stock_mappings.csv");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(","); //CSV 파일 형식에 맞게 구분자 수정
                    if (columns.length >= 3) {
                        String ticker = columns[0].trim();   //티커
                        List<String> temp = new ArrayList<>();
                        String exchange = columns[1].trim(); //거래소 코드
                        String company = columns[2].trim();  //회사명
                        temp.add(exchange);
                        temp.add(company);
                        stockExchangeMap.put(ticker, temp);
                    }
                }
            } catch (IOException e) {
                log.error("Error reading CSV file: {}", e.getMessage());
            }
        }
    }

    //티커로 거래소 코드를 반환
    public String getExchangeByTicker(String ticker) {
        try {
            return stockExchangeMap.get(ticker).get(0);
        } catch (Exception e) {
            throw new StockNotFound();
        }
    }

    public List<List<String>> searchTickers(String keyword) {
        List<List<String>> matchingTickers = new ArrayList<>();

        //우선 keyword와 정확히 일치하는 티커가 있는지 확인
        if (stockExchangeMap.containsKey(keyword)) {
            List<String> exactMatch = new ArrayList<>();
            exactMatch.add(keyword);
            exactMatch.addAll(stockExchangeMap.get(keyword));
            matchingTickers.add(exactMatch);
        }

        //정확한 일치가 없는 경우, keyword를 포함하는 티커를 검색
        for (String ticker : stockExchangeMap.keySet()) {
            if (ticker.equals(keyword)) {
                continue;
            }
            if (ticker.contains(keyword)) {
                List<String> temp = new ArrayList<>();
                temp.add(ticker);
                temp.addAll(stockExchangeMap.get(ticker));
                matchingTickers.add(temp);
            }
            if (matchingTickers.size() == 5) {
                break;
            }
        }

        if (matchingTickers.isEmpty()) {
            throw new StockNotFound();
        }

        //결과 정렬
        //matchingTickers.sort((o1, o2) -> o1.get(0).compareToIgnoreCase(o2.get(0)));

        return matchingTickers;
    }
}