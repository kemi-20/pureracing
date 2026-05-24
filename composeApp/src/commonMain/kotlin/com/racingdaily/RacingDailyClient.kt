package com.racingdaily

import androidx.compose.runtime.Composable
import com.racingdaily.di.initKoin
import org.koin.compose.KoinApplication

@Composable
fun RacingDailyClient() {
    KoinApplication(application = { modules(initKoin()) }) {
        App()
    }
}
