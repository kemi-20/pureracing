package com.racingdaily.ui.screens.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racingdaily.data.model.StationNavItem
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

data class SessionDetailUiState(
    val navbar: List<StationNavItem> = emptyList(),
    val scores: List<JsonObject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SessionDetailViewModel(
    private val api: ApiService,
    private val gpId: Int,
    private val sessionId: Int
) : ViewModel() {
    private val _state = MutableStateFlow(SessionDetailUiState())
    val state: StateFlow<SessionDetailUiState> = _state.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                val rank = api.getStationRank(gpId)
                val score = api.getStationScore(gpId)
                Pair(rank, score)
            }.onSuccess { (rank, score) ->
                _state.value = SessionDetailUiState(navbar = rank.navbar, scores = score)
            }.onFailure {
                _state.value = SessionDetailUiState(error = it.message)
            }
        }
    }
}
