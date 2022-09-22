package com.example.stockquotespring.controllers;

import com.example.stockquotespring.Quote;
import com.example.stockquotespring.config.StockQuoteAPI;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@RestController
public class QuoteController {
    private static final String TEST_SYMBOL = "TEST";
    private static final String FAIL_SYMBOL = "FAIL";
    private static final String SLOW_SYMBOL = "SLOW";
    private static final long SLOW_TIME = 60000;
    private static final long cache_interval = 60;
    private static final long MINUTE_IN_MILLISECONDS = 60000;
    private static Map<String, Quote> backUpCache;
    private static Map<String, Function<String, Quote>> stockQuoteTestFunctions;
    private final RedisTemplate<String, String> cachedQuotes;

    private final StockQuoteAPI stockQuoteAPI;


    @Autowired
    public QuoteController(RedisTemplate<String, String> cachedQuotes, StockQuoteAPI stockQuoteAPI) {
        this.cachedQuotes = cachedQuotes;
        this.stockQuoteAPI = stockQuoteAPI;
        if (backUpCache == null)
            backUpCache = new HashMap<>();
        if (stockQuoteTestFunctions == null)
            fillStockQuoteTestFunctions();
    }

    @GetMapping("/cached-quotes")
    public Quote[] getAllCachedQuotes() {
        var tmpQuotes = new ArrayList<Quote>();
        var keys = cachedQuotes.keys("*");
        keys.iterator()
                .forEachRemaining(x -> {
                    try {
                        tmpQuotes.add((new ObjectMapper()).readValue(x, Quote.class));
                    } catch (IOException e) {
                        log.error("Error during serializing quote objects in cached list");
                    }
                });
        return tmpQuotes.toArray(new Quote[]{});
    }

    @PostMapping("/{symbol}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void updateCache(@PathVariable String symbol, @RequestParam double price) {
        log.info("updating backup cache ");
        backUpCache.put(symbol,
                new Quote(symbol, price, new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
        );
    }

    @GetMapping("/{symbol}")
    public Quote getStockQuote(@PathVariable String symbol) throws IOException {
        var testStockQuoteFunction = stockQuoteTestFunctions.get(symbol.toLowerCase());
        if (testStockQuoteFunction != null)
            return testStockQuoteFunction.apply(symbol);
        return getStockQuoteFromAPI(symbol);
    }

    private void fillStockQuoteTestFunctions() {
        stockQuoteTestFunctions = new HashMap<>() {{
            put(TEST_SYMBOL.toLowerCase(), x -> Quote.getTestQuote());
            put(SLOW_SYMBOL.toLowerCase(), x -> Quote.getSlowQuote(SLOW_TIME));
            put(FAIL_SYMBOL.toLowerCase(), x -> {
                throw new RuntimeException("Failing as requested");
            });
        }};
    }

    private Quote getStockQuoteFromAPI(String symbol) throws IOException {
        if (cachedQuotes.hasKey(symbol)) {
            var quote = Quote.fromJson(cachedQuotes.opsForValue().get(symbol));
            if (quote.isStale(cache_interval * MINUTE_IN_MILLISECONDS)) {
                quote = stockQuoteAPI.getQuote(symbol);
                quote.setTime(System.currentTimeMillis());

                cachedQuotes.opsForValue().append(symbol, quote.toJson());
                backUpCache.put(symbol, quote);
                return quote;
            }
            return quote;
        }

        var quote = stockQuoteAPI.getQuote(symbol);
        cachedQuotes.opsForValue().append(symbol, quote.toJson());
        return quote;
    }


}
