package com.racingdaily

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.racingdaily.data.remote.ApiService
import com.racingdaily.data.remote.createHttpClient
import com.racingdaily.data.remote.newsReferer
import com.racingdaily.platform.currentLocalDateTimeParts
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header

@Composable
@OptIn(ExperimentalCoilApi::class)
fun RacingDailyClient() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
                add(KtorNetworkFetcherFactory(httpClient = HttpClient {
                    defaultRequest {
                        header("Referer", newsReferer)
                        header("Origin", newsReferer.trimEnd('/'))
                        header("User-Agent", "RacingDaily/1.3.0")
                    }
                }))
            }
            .build()
    }
    val client = remember { createHttpClient() }
    val api = remember(client) { ApiService(client) }
    val startupSeason = remember { currentLocalDateTimeParts().year }
    LaunchedEffect(api, startupSeason) {
        api.preloadHomeThenSecondary(startupSeason)
    }
    DisposableEffect(client) {
        onDispose { client.close() }
    }
    App(api)
}
