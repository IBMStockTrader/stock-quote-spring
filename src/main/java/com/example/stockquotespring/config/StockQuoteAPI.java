package com.example.stockquotespring.config;

import com.example.stockquotespring.Quote;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class StockQuoteAPI {

    public Quote getQuote(String symbol) {
        //todo
        return new Quote(symbol, 12, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }
}
