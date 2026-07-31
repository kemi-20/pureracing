package com.racingdaily.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ApiServiceTest {
    @Test
    fun forceRefreshFallsBackToPreviouslyCachedData() = runTest {
        var requestCount = 0
        val client = jsonClient {
            requestCount++
            if (requestCount == 1) {
                """{"code":200,"msg":"success","data":{"navbar":[{"id":1,"name":"Headlines"}]}}"""
            } else {
                """{"code":503,"msg":"temporarily unavailable"}"""
            }
        }
        try {
            val api = ApiService(client)
            assertEquals("Headlines", api.getNavTabs().navbar.single().name)
            assertEquals("Headlines", api.getNavTabs(forceRefresh = true).navbar.single().name)
            assertEquals(2, requestCount)
        } finally {
            client.close()
        }
    }

    @Test
    fun successfulEnvelopeWithoutDataReportsAUsefulError() = runTest {
        val client = jsonClient { """{"code":200,"msg":"success"}""" }
        try {
            val failure = assertFailsWith<IllegalArgumentException> {
                ApiService(client).getNavTabs()
            }
            assertTrue(failure.message.orEmpty().contains("did not contain data"))
        } finally {
            client.close()
        }
    }

    @Test
    fun articleCommentsLoadEveryTopLevelPage() = runTest {
        var requestCount = 0
        val client = jsonClient {
            requestCount++
            if (requestCount == 1) {
                """{"code":200,"msg":"success","data":{"comment_list":[{"id":1},{"id":2}],"page":0,"count":3}}"""
            } else {
                """{"code":200,"msg":"success","data":{"comment_list":[{"id":3}],"page":1,"count":3}}"""
            }
        }
        try {
            val comments = ApiService(client).getArticleCommentsWithReplies(articleId = 42)

            assertEquals(listOf(1, 2, 3), comments.comment_list.map { it.id })
            assertEquals(2, requestCount)
        } finally {
            client.close()
        }
    }

    @Test
    fun calendarRequestOverridesNewsOriginAndReferer() = runTest {
        var originHeaders = emptyList<String>()
        var refererHeaders = emptyList<String>()
        val engine = MockEngine { request ->
            originHeaders = request.headers.getAll(HttpHeaders.Origin).orEmpty()
            refererHeaders = request.headers.getAll(HttpHeaders.Referrer).orEmpty()
            respond(
                content = """
                    BEGIN:VEVENT
                    UID:f1#GP1_gp
                    DTSTART:20260301T000000Z
                    SUMMARY:F1: Grand Prix (Australian)
                    END:VEVENT
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/calendar")
            )
        }
        val client = HttpClient(engine) {
            defaultRequest {
                url("https://api.romielf.com/")
                headers.append(HttpHeaders.Origin, newsReferer.trimEnd('/'))
                headers.append(HttpHeaders.Referrer, newsReferer)
            }
        }
        try {
            ApiService(client).getF1Calendar(2026)

            assertEquals(listOf("https://motorsportcalendars.com"), originHeaders)
            assertEquals(listOf("https://motorsportcalendars.com/"), refererHeaders)
        } finally {
            client.close()
        }
    }

    private fun jsonClient(response: () -> String): HttpClient {
        val engine = MockEngine {
            respond(
                content = response(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(engine) {
            defaultRequest {
                url("https://api.romielf.com/")
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    explicitNulls = false
                })
            }
        }
    }
}
