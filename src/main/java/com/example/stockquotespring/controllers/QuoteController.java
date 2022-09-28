package com.example.stockquotespring.controllers;

import com.example.stockquotespring.Quote;
import com.example.stockquotespring.config.StockQuoteAPI;
import com.example.stockquotespring.encrypt.Encryptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@RestController
public class QuoteController {
    private static final String TEST_SYMBOL = "TEST";
    private static final String FAIL_SYMBOL = "FAIL";
    private static final String SLOW_SYMBOL = "SLOW";
    private static final long SLOW_TIME = 60000;
    private static Map<String, Quote> backUpCache;
    private static Map<String, Function<String, Quote>> stockQuoteTestFunctions;
    private final RedisTemplate<String, String> cachedQuotes;
    private final StockQuoteAPI stockQuoteAPI;
    private final Encryptor encryptor;
    @Value("${app.redis.ttl-seconds}")
    private long cacheDuration;

    @Autowired
    public QuoteController(RedisTemplate<String, String> cachedQuotes, StockQuoteAPI stockQuoteAPI,
                           Encryptor encryptor) {
        this.cachedQuotes = cachedQuotes;
        this.stockQuoteAPI = stockQuoteAPI;
        this.encryptor = encryptor;
        if (backUpCache == null)
            backUpCache = new HashMap<>();
        if (stockQuoteTestFunctions == null)
            fillStockQuoteTestFunctions();
    }

    @GetMapping("/")
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
    public Quote getStockQuote(@PathVariable String symbol) throws Exception {
        var testStockQuoteFunction = stockQuoteTestFunctions.get(symbol.toLowerCase());
        if (testStockQuoteFunction != null)
            return testStockQuoteFunction.apply(symbol);
        return getStockQuoteFromAPI(symbol);
    }

    private Quote getStockQuoteFromAPI(String symbol) throws Exception {
        var isInRedis = Boolean.TRUE.equals(cachedQuotes.hasKey(symbol));
        if (isInRedis) {
            log.info("Getting quote from redis - symbol: " + symbol);
            return Quote.fromJson(encryptor.decrypt(getFromRedis(symbol)));
        }
        var quote = stockQuoteAPI.getQuote(symbol);
        setInRedisWithTimeout(symbol, encryptor.encrypt(quote.toJson()));
        return quote;
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

    private void setInRedisWithTimeout(String key, String input) {
        cachedQuotes.opsForValue().set(key, input, cacheDuration, TimeUnit.SECONDS);
    }

    private String getFromRedis(String key) {
        return cachedQuotes.opsForValue().get(key);
    }
}
