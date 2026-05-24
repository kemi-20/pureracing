package com.racingdaily.di

import com.racingdaily.data.remote.ApiService
import com.racingdaily.data.remote.createHttpClient
import org.koin.core.module.dsl.single
import org.koin.dsl.module

val appModule = module {
    single { createHttpClient() }
    single { ApiService(get()) }
}
