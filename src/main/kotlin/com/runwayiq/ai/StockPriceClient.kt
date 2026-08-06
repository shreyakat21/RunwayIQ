package com.runwayiq.ai

import com.runwayiq.data.model.StockQuote
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class StockApiException(message: String) : Exception(message)

class StockPriceClient(apiKey: String) {

    private val apiKey = apiKey.trim()

    private val client = HttpClient(CIO) {
        engine { requestTimeout = 15_000 }
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val endpoint = "https://finnhub.io/api/v1/quote"

    suspend fun getQuote(ticker: String): StockQuote {
        val symbol = ticker.trim().uppercase()
        val response = client.get(endpoint) {
            parameter("symbol", symbol)
            parameter("token", apiKey)
        }
        val rawBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw StockApiException("Failed to fetch $symbol: ${response.status}")
        }

        val obj = try {
            json.parseToJsonElement(rawBody).jsonObject
        } catch (e: Exception) {
            throw StockApiException("Unexpected response for $symbol")
        }

        val price = obj["c"]?.jsonPrimitive?.doubleOrNull
            ?: throw StockApiException("No price data for $symbol")
        if (price <= 0.0) {
            throw StockApiException("Unknown ticker: $symbol")
        }
        val changePct = obj["dp"]?.jsonPrimitive?.doubleOrNull ?: 0.0

        return StockQuote(ticker = symbol, priceCents = (price * 100).toLong(), changePct = changePct)
    }

    /** Fetches quotes for each ticker independently; tickers that fail to resolve are simply omitted. */
    suspend fun getQuotes(tickers: List<String>): List<StockQuote> =
        tickers.map { it.trim().uppercase() }.distinct().mapNotNull { ticker ->
            try {
                getQuote(ticker)
            } catch (e: Exception) {
                null
            }
        }

    fun close() = client.close()
}
