package com.racingdaily.di

import com.racingdaily.data.remote.ApiService
import com.racingdaily.data.remote.createHttpClient
import com.racingdaily.ui.screens.home.HomeViewModel
import com.racingdaily.ui.screens.news.NewsDetailViewModel
import com.racingdaily.ui.screens.race.RaceViewModel
import com.racingdaily.ui.screens.race.SessionDetailViewModel
import com.racingdaily.ui.screens.rankings.RankingViewModel
import com.racingdaily.ui.screens.more.MoreViewModel
import com.racingdaily.ui.screens.track.TrackViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun initKoin() = module {
    single { createHttpClient() }
    single { ApiService(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { params -> NewsDetailViewModel(get(), params.get()) }
    viewModel { RaceViewModel(get()) }
    viewModel { params -> SessionDetailViewModel(get(), params.get(), params.get()) }
    viewModel { RankingViewModel(get()) }
    viewModel { MoreViewModel(get()) }
    viewModel { params -> TrackViewModel(get(), params.get()) }
}
