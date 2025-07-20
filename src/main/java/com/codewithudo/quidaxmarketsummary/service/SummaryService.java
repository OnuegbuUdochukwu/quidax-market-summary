package com.codewithudo.quidaxmarketsummary.service;

import com.codewithudo.quidaxmarketsummary.dto.MarketData;
import com.codewithudo.quidaxmarketsummary.dto.MarketSummary;
import com.codewithudo.quidaxmarketsummary.dto.QuidaxResponse;
import com.codewithudo.quidaxmarketsummary.dto.Ticker;
import lombok.Data;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SummaryService {

    private static final String QUIDAX_API_TICKERS_URL = "https://app.quidax.io/api/v1/markets/tickers";
    private final RestTemplate restTemplate;

    public SummaryService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches market data and transforms it into a list of summaries.
     * @return A list of MarketSummary objects.
     */
    public List<MarketSummary> getMarketSummaries() {
        // 1. Tell RestTemplate to expect the full QuidaxResponse object
        ResponseEntity<QuidaxResponse> response = restTemplate.exchange(
                QUIDAX_API_TICKERS_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        QuidaxResponse body = response.getBody();
        List<MarketSummary> summaries = new ArrayList<>();

        if (body != null && "success".equals(body.getStatus())) {
            // 2. Get the map of MarketData from the response
            Map<String, MarketData> marketDataMap = body.getData();

            for (Map.Entry<String, MarketData> entry : marketDataMap.entrySet()) {
                String marketName = entry.getKey();
                // 3. Unwrap the final Ticker object
                Ticker ticker = entry.getValue().getTicker();

                if (ticker != null) {
                    MarketSummary summary = new MarketSummary();
                    summary.setMarket(marketName.replace("_", "/").toUpperCase());
                    summary.setPrice(ticker.getPrice());
                    summary.setVolume(ticker.getVolume());
                    summary.setHigh(ticker.getHigh());
                    summary.setLow(ticker.getLow());
                    summary.setPriceChangePercent(
                            calculatePriceChangePercent(ticker.getPrice(), ticker.getOpen())
                    );
                    summaries.add(summary);
                }
            }
        }
        return summaries;
    }

    /**
     * Calculates the 24-hour price change percentage.
     * @param lastPrice The current price.
     * @param openPrice The price 24 hours ago.
     * @return A formatted string representing the percentage change (e.g., "+2.15%").
     */
    private String calculatePriceChangePercent(String lastPrice, String openPrice) {
        try {
            BigDecimal last = new BigDecimal(lastPrice);
            BigDecimal open = new BigDecimal(openPrice);

            if (open.compareTo(BigDecimal.ZERO) == 0) {
                return "0.00%"; // Avoid division by zero.
            }

            BigDecimal change = last.subtract(open);
            BigDecimal percentChange = change.divide(open, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            return String.format("%+.2f%%", percentChange); // Formats to 2 decimal places with a sign.
        } catch (NumberFormatException | ArithmeticException e) {
            // If price data is invalid or causes an error, return a neutral value.
            return "0.00%";
        }
    }

    /**
     * Inner class to model the exact structure of the Quidax tickers response.
     * This keeps the external API's structure separate from our application's DTO.
     */
}