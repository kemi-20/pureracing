package com.racingdaily

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.racingdaily.data.remote.ApiService
import com.racingdaily.di.appModule
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun RacingDailyClient() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = HttpClient {
                    defaultRequest {
                        header("Referer", "https://news.romielf.com")
                    }
                }))
            }
            .build()
    }
    KoinApplication(application = { modules(appModule) }) {
        val api = koinInject<ApiService>()
        App(api)
    }
}
