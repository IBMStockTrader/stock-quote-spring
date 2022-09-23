package com.example.stockquotespring.config;

import com.example.stockquotespring.Quote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class StockQuoteAPI {

    public Quote getQuote(String symbol) {
        //todo
        log.info("Getting stock quote from API");
        return new Quote(symbol, 12, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }
}
