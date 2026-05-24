package com.racingdaily

import androidx.compose.runtime.Composable
import com.racingdaily.di.initKoin
import org.koin.compose.KoinApplication
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.racingdaily.data.remote.createCoilHttpClient

@Composable
fun RacingDailyClient() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = createCoilHttpClient()))
            }
            .build()
    }

    KoinApplication(application = { modules(initKoin()) }) {
        App()
    }
}
