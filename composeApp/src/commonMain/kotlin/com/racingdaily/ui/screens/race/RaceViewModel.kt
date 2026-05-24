package com.racingdaily.ui.screens.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RaceUiState(
    val races: List<RaceGp> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RaceViewModel(private val api: ApiService) : ViewModel() {
    private val _state = MutableStateFlow(RaceUiState())
    val state: StateFlow<RaceUiState> = _state.asStateFlow()

    init { loadRaces() }

    fun loadRaces() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { api.getRaceSchedule() }
                .onSuccess { _state.value = RaceUiState(races = it) }
                .onFailure { _state.value = RaceUiState(error = it.message) }
        }
    }
}
