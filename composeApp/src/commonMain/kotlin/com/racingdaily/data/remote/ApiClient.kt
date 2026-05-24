package com.racingdaily.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val newsReferer = "https://news.romielf.com/"

fun createHttpClient() = HttpClient {
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
        header("User-Agent", "RacingDaily/1.2.9")
        header("Referer", newsReferer)
    }
}
