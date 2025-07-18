package com.codewithudo.quidaxmarketsummary.service;

import com.codewithudo.quidaxmarketsummary.dto.MarketSummary;
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
        // Fetch the raw data from the Quidax API.
        ResponseEntity<Map<String, QuidaxTicker>> response = restTemplate.exchange(
                QUIDAX_API_TICKERS_URL,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, QuidaxTicker> tickers = response.getBody();
        List<MarketSummary> summaries = new ArrayList<>();

        if (tickers != null) {
            // Iterate over each market ticker returned by the API.
            for (Map.Entry<String, QuidaxTicker> entry : tickers.entrySet()) {
                String marketName = entry.getKey();
                QuidaxTicker tickerData = entry.getValue();

                // Create a new summary object for our response.
                MarketSummary summary = new MarketSummary();
                summary.setMarket(marketName.replace("_", "/").toUpperCase());
                summary.setPrice(tickerData.getLast());
                summary.setVolume(tickerData.getVol());
                summary.setHigh(tickerData.getHigh());
                summary.setLow(tickerData.getLow());

                // Calculate the 24h percentage change.
                summary.setPriceChangePercent(
                        calculatePriceChangePercent(tickerData.getLast(), tickerData.getOpen())
                );

                summaries.add(summary);
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
    @Data
    private static class QuidaxTicker {
        private String open;
        private String low;
        private String high;
        private String last;
        private String vol;
    }
}