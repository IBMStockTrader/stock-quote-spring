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

package com.ibm.hybrid.cloud.sample.stocktrader.stockquote.config;

import com.ibm.hybrid.cloud.sample.stocktrader.stockquote.Quote;
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
        try {
            return new Quote(symbol, 12.4, "2021-02-01");
//            return restTemplate.getForObject(stockQuoteApiUrl, Quote.class, symbol);
        } catch (Exception e) {
            log.error("Error during GET to stock quote API " + stockQuoteApiUrl + " - returning test quote");
            return Quote.getTestQuote();
        }
    }
}
