package com.pureracing.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pureracing.app.data.model.*
import com.pureracing.app.data.repository.RacingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

@HiltViewModel
class HomeViewModel @Inject constructor(private val repo: RacingRepository) : ViewModel() {
    private val _seasons = MutableStateFlow<UiState<List<Season>>>(UiState.Loading)
    val seasons = _seasons.asStateFlow()

    private val _schedule = MutableStateFlow<UiState<List<RaceSchedule>>>(UiState.Loading)
    val schedule = _schedule.asStateFlow()

    init { loadSeasons() }

    fun loadSeasons() = viewModelScope.launch {
        _seasons.value = UiState.Loading
        runCatching { repo.getSeasons() }
            .onSuccess { _seasons.value = if (it.code == 200) UiState.Success(it.data ?: emptyList()) else UiState.Error(it.msg) }
            .onFailure { _seasons.value = UiState.Error(it.message ?: "未知错误") }
    }

    fun loadSchedule(seasonId: Int) = viewModelScope.launch {
        _schedule.value = UiState.Loading
        runCatching { repo.getSchedule(seasonId) }
            .onSuccess { _schedule.value = if (it.code == 200) UiState.Success(it.data ?: emptyList()) else UiState.Error(it.msg) }
            .onFailure { _schedule.value = UiState.Error(it.message ?: "未知错误") }
    }
}

@HiltViewModel
class RankViewModel @Inject constructor(private val repo: RacingRepository) : ViewModel() {
    private val _driverRank = MutableStateFlow<UiState<List<RankItem>>>(UiState.Loading)
    val driverRank = _driverRank.asStateFlow()

    private val _constructorRank = MutableStateFlow<UiState<List<RankItem>>>(UiState.Loading)
    val constructorRank = _constructorRank.asStateFlow()

    fun loadRankings(seasonId: Int) {
        viewModelScope.launch {
            runCatching { repo.getRankings(seasonId, "driver") }
                .onSuccess { _driverRank.value = if (it.code == 200) UiState.Success(it.data ?: emptyList()) else UiState.Error(it.msg) }
                .onFailure { _driverRank.value = UiState.Error(it.message ?: "未知错误") }
        }
        viewModelScope.launch {
            runCatching { repo.getRankings(seasonId, "constructor") }
                .onSuccess { _constructorRank.value = if (it.code == 200) UiState.Success(it.data ?: emptyList()) else UiState.Error(it.msg) }
                .onFailure { _constructorRank.value = UiState.Error(it.message ?: "未知错误") }
        }
    }
}

@HiltViewModel
class NewsViewModel @Inject constructor(private val repo: RacingRepository) : ViewModel() {
    private val _news = MutableStateFlow<UiState<List<NewsItem>>>(UiState.Loading)
    val news = _news.asStateFlow()

    init { loadNews() }

    fun loadNews() = viewModelScope.launch {
        _news.value = UiState.Loading
        runCatching { repo.getNews() }
            .onSuccess { _news.value = if (it.code == 200) UiState.Success(it.data?.list ?: emptyList()) else UiState.Error(it.msg) }
            .onFailure { _news.value = UiState.Error(it.message ?: "未知错误") }
    }
}

@HiltViewModel
class AuthViewModel @Inject constructor(private val repo: RacingRepository) : ViewModel() {
    private val _loginState = MutableStateFlow<UiState<LoginData>?>(null)
    val loginState = _loginState.asStateFlow()

    fun login(phone: String, password: String) = viewModelScope.launch {
        _loginState.value = UiState.Loading
        runCatching { repo.login(phone, password) }
            .onSuccess { _loginState.value = if (it.code == 200 && it.data != null) UiState.Success(it.data) else UiState.Error(it.msg) }
            .onFailure { _loginState.value = UiState.Error(it.message ?: "登录失败") }
    }
}
