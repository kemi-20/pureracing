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
import com.racingdaily.platform.currentLocalDateTimeParts

@Composable
@OptIn(ExperimentalCoilApi::class)
fun RacingDailyClient() {
    val client = remember { createHttpClient() }
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
                add(KtorNetworkFetcherFactory(httpClient = client))
            }
            .build()
    }
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
