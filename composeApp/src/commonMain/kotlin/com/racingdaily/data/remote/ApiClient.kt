package com.racingdaily.data.remote

import com.racingdaily.platform.appVersionName
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val newsReferer = "https://news.romielf.com/"
private val retryableHttpStatuses = setOf(408, 425, 429)

fun createHttpClient() = HttpClient {
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
    install(HttpRequestRetry) {
        maxRetries = 2
        retryIf { _, response ->
            response.status.value in retryableHttpStatuses || response.status.value >= 500
        }
        retryOnExceptionIf { _, _ -> true }
        exponentialDelay()
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            explicitNulls = false
        })
    }
    defaultRequest {
        url("https://api.romielf.com/")
        header("User-Agent", "RacingDaily/$appVersionName")
        header("Referer", newsReferer)
        header("Origin", newsReferer.trimEnd('/'))
    }
}
