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

package com.ibm.hybrid.cloud.sample.stocktrader.stockquote;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(value = "time")
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
