package com.codewithudo.quidaxmarketsummary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Ticker {
    private String open;
    private String low;
    private String high;
    @JsonProperty("last")
    private String price;
    @JsonProperty("vol")
    private String volume;
}