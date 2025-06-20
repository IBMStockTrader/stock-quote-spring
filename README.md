# stock-quote-spring

Spring implementation of the Stock Quote microservice. The microservice grabs stock quotes from an API. It responds to a `GET /{symbol}` request and returns a JSON object containing that symbol, the price, the date and the time it was quoted.

For example, if you hit the `http://localhost:9080/stock-quote/IBM` URL, it would return `{"symbol": "IBM", "price": 155.23, "date": "2016-06-27", "time": 1467028800000}`.

This service uses Redis for caching. When a quote is requested, it first checks to see if it is in the cache, and if so, whether it is less than an hour old, and if so, just uses that. Otherwise (or if any exceptions occur communicating with Redis), it drives the REST call to the stock quote API as usual, then caches it to the Redis.