package com.codewithudo.quidaxmarketsummary.controller;

import com.codewithudo.quidaxmarketsummary.dto.MarketSummary;
import com.codewithudo.quidaxmarketsummary.service.SummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @GetMapping("/summary")
    public List<MarketSummary> getMarketSummaries() {
        return summaryService.getMarketSummaries();
    }
}
