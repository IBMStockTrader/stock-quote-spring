/*
       Copyright 2022 Kyndryl, All Rights Reserved
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.ibm.hybrid.cloud.sample.stocktrader.stockquote.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.hybrid.cloud.sample.stocktrader.stockquote.Quote;
import com.ibm.hybrid.cloud.sample.stocktrader.stockquote.config.StockQuoteAPI;
import com.ibm.hybrid.cloud.sample.stocktrader.stockquote.encrypt.Encryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@RestController
@RequestMapping("stock-quote")
public class QuoteController {

    private static final String TEST_SYMBOL = "TEST";
    private static final String FAIL_SYMBOL = "FAIL";
    private static final String SLOW_SYMBOL = "SLOW";
    private static final long SLOW_TIME = 60000;
    private static Map<String, Function<String, Quote>> stockQuoteTestFunctions;
    private final RedisTemplate<String, String> cachedQuotes;
    private final StockQuoteAPI stockQuoteAPI;
    private final Encryptor encryptor;
    private static String dummyText = "dummy test";
    @Value("${app.redis.ttl-seconds}")
    private long cacheDuration;

    @Autowired
    public QuoteController(RedisTemplate<String, String> cachedQuotes, StockQuoteAPI stockQuoteAPI,
                           Encryptor encryptor, Environment env) throws IOException {
        this.cachedQuotes = cachedQuotes;
        this.stockQuoteAPI = stockQuoteAPI;
        this.encryptor = encryptor;
        configTestQuoteFunctions();
        setDummyText(env);
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
            return Quote.fromJson(
                    encryptor.decrypt(getFromRedis(symbol))
            );
        }
        var quote = stockQuoteAPI.getQuote(symbol);
        quote.setDummy(dummyText);
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

    private void configTestQuoteFunctions() {
        if (stockQuoteTestFunctions == null)
            fillStockQuoteTestFunctions();
    }

    private void setDummyText(Environment env) throws IOException {
        if (env.getProperty("app.use-dummy-text") != null && env.getProperty("app.use-dummy-text").equals("true")) {
            var dummyTextStream = getClass().getClassLoader().getResourceAsStream("static/lorem.txt");
            dummyText = new String(dummyTextStream.readAllBytes());
        }
    }
}
