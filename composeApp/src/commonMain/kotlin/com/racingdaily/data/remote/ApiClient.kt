package com.racingdaily.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            coerceInputValues = true
        })
    }
    install(Logging) {
        level = LogLevel.NONE
    }
    defaultRequest {
        url("https://api.romielf.com/")
        header("User-Agent", "RacingDaily/1.2.9")
        header("Accept", "application/json")
    }
}

fun createCoilHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
            coerceInputValues = true
        })
    }
    install(Logging) {
        level = LogLevel.NONE
    }
    defaultRequest {
        header("User-Agent", "RacingDaily/1.2.9")
        header("Referer", "https://oss.static.romielf.com")
    }
}
