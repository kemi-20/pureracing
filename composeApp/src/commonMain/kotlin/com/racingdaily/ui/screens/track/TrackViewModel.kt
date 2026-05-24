package com.racingdaily.ui.screens.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racingdaily.data.model.TrackData
import com.racingdaily.data.model.TrackScoreData
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrackUiState(
    val trackInfo: TrackData? = null,
    val trackScore: TrackScoreData? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TrackViewModel(private val api: ApiService, private val trackId: Int) : ViewModel() {
    private val _state = MutableStateFlow(TrackUiState())
    val state: StateFlow<TrackUiState> = _state.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                val info = api.getTrackInfo(trackId)
                val score = api.getTrackScore(trackId)
                Pair(info, score)
            }.onSuccess { (info, score) ->
                _state.value = TrackUiState(trackInfo = info, trackScore = score)
            }.onFailure {
                _state.value = TrackUiState(error = it.message)
            }
        }
    }
}
