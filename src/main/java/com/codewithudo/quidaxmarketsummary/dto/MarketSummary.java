package com.codewithudo.quidaxmarketsummary.dto;

import lombok.Data;

@Data
public class MarketSummary {
    private String market;
    private String price;
    private String volume;
    private String high;
    private String low;
    private String priceChangePercent;
}