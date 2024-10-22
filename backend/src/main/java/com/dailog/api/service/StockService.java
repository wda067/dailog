package com.dailog.api.service;

import com.dailog.api.exception.InvalidRequest;
import com.dailog.api.response.stock.StockDetail;
import com.dailog.api.response.stock.StockInfo;
import com.dailog.api.response.stock.StockChart;
import com.dailog.api.response.stock.StockSearch;
import com.dailog.api.util.CsvUtil;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private static final String API_URL = "https://openapi.koreainvestment.com:9443";
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CsvUtil csvUtil;
    @Value("${korea-investment.app-key}")
    private String appKey;
    @Value("${korea-investment.app-secret}")
    private String appSecret;

    public void getAccessToken() {
        String url = API_URL + "/oauth2/tokenP";

        //JSON 바디 생성
        JSONObject requestBody = new JSONObject();
        requestBody.put("grant_type", "client_credentials");
        requestBody.put("appkey", appKey);
        requestBody.put("appsecret", appSecret);

        //헤더 설정 (Content-Type: application/json)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //JSON 바디와 헤더를 함께 HttpEntity에 담아서 요청
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            //상태 코드 확인 후 성공 시 반환
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String accessToken = jsonResponse.getString("access_token");
                int expiresIn = jsonResponse.getInt("expires_in");
                storeAccessToken(accessToken, expiresIn - 60);
            } else {
                log.info("Failed to get access token. Status code: {}", response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.info("Error occurred while fetching access token: {}", e.getMessage());
        }
    }

    public void storeAccessToken(String token, int expiresIn) {
        redisTemplate.opsForValue().set("accessToken", token, expiresIn, TimeUnit.SECONDS);
    }

    /**
     * 해외주식 현재가 상세 API
     * 시가, 고가, 저가, 현재가, 전일종가, 시가총액, 52주 최고가/최저가, 최고일자/최저일자
     * PER, PBR, EPS, BPS, 자본금, 업종
     * @param ticker 주식 티커
     */
    public StockDetail getStockDetail(String ticker) {
        String exchangeCode = csvUtil.getExchangeByTicker(ticker);
        String url = API_URL + "/uapi/overseas-price/v1/quotations/price-detail" +
                "?AUTH=" + "&EXCD=" + exchangeCode + "&SYMB=" + ticker;

        Object accessToken = getAccessTokenFromRedis();
        HttpHeaders headers = getHttpHeaders(accessToken, "HHDFS76200200");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                JSONObject output = jsonResponse.getJSONObject("output");

                String last = output.getString("last");  //현재가
                String base = output.getString("base");  //전일 종가

                double lastPrice = Double.parseDouble(last);
                String formattedLast = String.format("%.2f", lastPrice);
                double basePrice = Double.parseDouble(base);
                double diffValue = lastPrice - basePrice;
                String diff = String.format("%.2f", diffValue);

                double rateValue = (diffValue / basePrice) * 100;
                String rate = String.format("%.2f", rateValue);

                String marketCap = output.getString("tomv");  //시가총액
                String formattedMarketCap = formatLargeNumber(marketCap);

                String high52Weeks = output.getString("h52p");  //52주 최고가
                String low52Weeks = output.getString("l52p");  //52주 최저가
                double highDouble = Double.parseDouble(high52Weeks);
                double lowDouble = Double.parseDouble(low52Weeks);
                String formattedHigh = String.format("%.2f", highDouble);
                String formattedLow = String.format("%.2f", lowDouble);

                String high52WeeksDate = output.getString("h52d");  //52주 최고일자
                String low52WeeksDate = output.getString("l52d");  //52주 최저일자
                String per = output.getString("perx");  //PER
                String pbr = output.getString("pbrx");  //PBR
                String eps = output.getString("epsx");  //EPS
                String bps = output.getString("bpsx");  //BPS
                String totalShares = output.getString("shar");  //상장주수
                DecimalFormat formatter = new DecimalFormat("#,###");
                String formattedShares = formatter.format(Long.parseLong(totalShares));

                String sector = output.getString("e_icod");  //업종

                return StockDetail.builder()
                        .last(formattedLast)
                        .diff(diff)
                        .rate(rate)
                        .marketCap(formattedMarketCap)
                        .high52Weeks(formattedHigh)
                        .high52WeeksDate(high52WeeksDate)
                        .low52Weeks(formattedLow)
                        .low52WeeksDate(low52WeeksDate)
                        .per(per)
                        .pbr(pbr)
                        .eps(eps)
                        .bps(bps)
                        .totalShares(formattedShares)
                        .sector(sector)
                        .build();
            } else {
                log.info("Failed to get stock data. Status code: {}", response.getStatusCode());
                throw new InvalidRequest();
            }
        } catch (RestClientException e) {
            log.error("Error occurred while fetching stock data: {}", e.getMessage());
        }
        throw new InvalidRequest();
    }

    /**
     * 해외주식 상품기본정보
     * @param ticker 주식 티커
     * @return 국가, 거래소 코드, 회사명
     */
    public StockInfo getStockInfo(String ticker) {
        String exchangeCode = csvUtil.getExchangeByTicker(ticker);
        String typeCode = switch (exchangeCode) {
            case "NAS" -> "512";
            case "NYS" -> "513";
            case "AMS" -> "529";
            default -> "";
        };
        String url = API_URL + "/uapi/overseas-price/v1/quotations/search-info" +
                "?PRDT_TYPE_CD=" + typeCode + "&PDNO=" + ticker;

        Object accessToken = getAccessTokenFromRedis();
        HttpHeaders headers = getHttpHeaders(accessToken, "CTPF1702R");
        headers.set("custtype", "P");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                JSONObject output = jsonResponse.getJSONObject("output");

                String country = output.getString("natn_name");
                String market = output.getString("tr_mket_name");
                String name = output.getString("prdt_name");

                return StockInfo.builder()
                        .country(country)
                        .market(market)
                        .name(name)
                        .build();
            } else {
                log.info("Failed to get stock data. Status code: {}", response.getStatusCode());
                throw new InvalidRequest();
            }
        } catch (RestClientException e) {
            log.error("Error occurred while fetching stock data: {}", e.getMessage());
        }
        throw new InvalidRequest();
    }


    /**
     * 해외주식 기간별 시세 API
     * @param ticker 주식 티커
     * @param dateType 일/주/월 구분 (0/1/2)
     * @param searchDate 조회기준일자
     */
    public List<StockChart> getStockHistory(String ticker, String dateType, String searchDate) {
        String exchangeCode = csvUtil.getExchangeByTicker(ticker);
        String url = API_URL + "/uapi/overseas-price/v1/quotations/dailyprice" +
                "?AUTH=" + "&EXCD=" + exchangeCode + "&SYMB=" + ticker + "&GUBN=" + dateType +
                "&BYMD=" + searchDate + "&MODP=1";

        Object accessToken = getAccessTokenFromRedis();
        HttpHeaders headers = getHttpHeaders(accessToken, "HHDFS76240000");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                JSONArray jsonArray = jsonResponse.getJSONArray("output2");

                List<StockChart> stockCharts = new ArrayList<>();
                jsonArray.forEach(item -> {
                    JSONObject stockData = (JSONObject) item;

                    String date = stockData.getString("xymd");  //일자(YYYYMMDD)
                    String diff = stockData.getString("diff");  //전일과 종가 차이
                    String volume = stockData.getString("tvol");  //거래량

                    String open = String.format("%.2f", Float.parseFloat(stockData.getString("open")));
                    String close = String.format("%.2f", Float.parseFloat(stockData.getString("clos")));
                    String high = String.format("%.2f", Float.parseFloat(stockData.getString("high")));
                    String low = String.format("%.2f", Float.parseFloat(stockData.getString("low")));
                    String rate = String.format("%.2f", Float.parseFloat(stockData.getString("rate")));

                    StockChart stockChart = StockChart.builder()
                            .date(date)
                            .open(open)
                            .close(close)
                            .high(high)
                            .low(low)
                            .diff(diff)
                            .rate(rate)
                            .volume(volume)
                            .build();
                    stockCharts.add(stockChart);
                });
                return stockCharts;
            } else {
                log.info("Failed to get stock data. Status code: {}", response.getStatusCode());
                throw new InvalidRequest();
            }
        } catch (RestClientException e) {
            log.error("Error occurred while fetching stock data: {}", e.getMessage());
        }
        throw new InvalidRequest();
    }

    /**
     * 해외주식 기간별 시세 API
     * @param ticker 주식 티커
     * @param intervalMinutes 분단위(1: 1분봉, 2: 2분봉, ...)
     */
    public List<StockChart> getStockPriceByMinutes(String ticker, String intervalMinutes) {
        String exchangeCode = csvUtil.getExchangeByTicker(ticker);
        String url = API_URL + "/uapi/overseas-price/v1/quotations/inquire-time-indexchartprice" +
                "?AUTH=" + "&EXCD=" + exchangeCode + "&SYMB=" + ticker + "&NMIN=" + intervalMinutes + "&PINC=1" +
                "&NEXT=" + "&NREC=100" + "&FILL=" + "&KEYB=";

        Object accessToken = getAccessTokenFromRedis();
        HttpHeaders headers = getHttpHeaders(accessToken, "HHDFS76950200");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                JSONArray jsonArray = jsonResponse.getJSONArray("output2");

                List<StockChart> stockCharts = new ArrayList<>();
                jsonArray.forEach(item -> {
                    JSONObject stockData = (JSONObject) item;

                    String date = stockData.getString("kymd");
                    String time = stockData.getString("khms");

                    String open = String.format("%.2f", Float.parseFloat(stockData.getString("open")));
                    String close = String.format("%.2f", Float.parseFloat(stockData.getString("last")));
                    String high = String.format("%.2f", Float.parseFloat(stockData.getString("high")));
                    String low = String.format("%.2f", Float.parseFloat(stockData.getString("low")));

                    StockChart stockChart = StockChart.builder()
                            .date(date + time)
                            .open(open)
                            .close(close)
                            .high(high)
                            .low(low)
                            .build();
                    stockCharts.add(stockChart);
                });
                return stockCharts;
            } else {
                log.info("Failed to get stock data. Status code: {}", response.getStatusCode());
                throw new InvalidRequest();
            }
        } catch (RestClientException e) {
            log.error("Error occurred while fetching stock data: {}", e.getMessage());
        }
        throw new InvalidRequest();
    }

    public List<StockSearch> searchTickers(String searchQuery) {
        return csvUtil.searchTickers(searchQuery.toUpperCase()).stream()
                .map(stock -> StockSearch.builder()
                        .ticker(stock.get(0))
                        .exchange(stock.get(1))
                        .name(stock.get(2))
                        .build())
                .toList();
    }

    private Object getAccessTokenFromRedis() {
        Object accessToken = redisTemplate.opsForValue().get("accessToken");

        if (accessToken == null) {
            //토큰 발급 시 중복 요청을 방지하기 위한 동기화 블록
            synchronized (this) {
                accessToken = redisTemplate.opsForValue().get("accessToken");
                if (accessToken == null) {
                    log.info("Access token not found. Fetching new token.");
                    getAccessToken();
                    accessToken = redisTemplate.opsForValue().get("accessToken");
                }
            }
        }

        if (accessToken == null) {
            throw new InvalidRequest();
        }

        return accessToken;
    }


    //private Object getAccessTokenFromRedis() {
    //    Object accessToken = redisTemplate.opsForValue().get("accessToken");
    //    if (accessToken == null) {
    //        log.info("Access token not found. Fetching new token.");
    //        getAccessToken();
    //        accessToken = redisTemplate.opsForValue().get("accessToken");
    //    }
    //
    //    if (accessToken == null) {
    //        throw new InvalidRequest();
    //    }
    //
    //    return accessToken;
    //}

    private HttpHeaders getHttpHeaders(Object accessToken, String tradingId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authorization", "Bearer " + accessToken);
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", tradingId);
        return headers;
    }

    public String formatLargeNumber(String strNumber) {
        long TRILLION = 1_000_000_000_000L;
        long BILLION = 1_000_000_000L;
        long MILLION = 1_000_000L;
        long number = Long.parseLong(strNumber);
        if (number >= TRILLION) {
            return String.format("%.2fT", number / (double) TRILLION);
        } else if (number >= BILLION) {
            return String.format("%.2fB", number / (double) BILLION);
        } else if (number >= MILLION) {
            return String.format("%.2fM", number / (double) MILLION);
        } else {
            return String.valueOf(number);
        }
    }}
