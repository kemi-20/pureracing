package com.racingdaily

import androidx.compose.runtime.Composable
import com.racingdaily.data.remote.ApiService
import com.racingdaily.di.appModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun RacingDailyClient() {
    KoinApplication(application = { modules(appModule) }) {
        val api = koinInject<ApiService>()
        App(api)
    }
}
