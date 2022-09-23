package com.example.stockquotespring.config;

import com.example.stockquotespring.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class StockQuoteAPI {
    private final RestTemplate restTemplate;
    @Value("${app.stock-quote-api.url}")
    private String stockQuoteApiUrl;
    @Autowired
    public StockQuoteAPI(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Quote getQuote(String symbol) {
        log.info("Getting stock quote from API");
        return restTemplate.getForObject(stockQuoteApiUrl, Quote.class, symbol);
    }
}
