package com.example.stockquotespring;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    private static final String TEST_SYMBOL = "TEST";
    private static final double TEST_PRICE = 123.45;
    private String symbol;
    private double price;
    private String date;

    public static Quote getSlowQuote(long slowTime) {
        try {
            Thread.sleep(slowTime);
        } catch (Exception e) {
            log.error("Error getting slow quote");
        }
        return getTestQuote();
    }

    public static Quote getTestQuote() {
        return new Quote(TEST_SYMBOL, TEST_PRICE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    public static Quote fromJson(String s) throws IOException {
        return (new ObjectMapper()).readValue(s, Quote.class);
    }

    public String toJson() throws JsonProcessingException {
        return (new ObjectMapper()).writeValueAsString(this);
    }
}
