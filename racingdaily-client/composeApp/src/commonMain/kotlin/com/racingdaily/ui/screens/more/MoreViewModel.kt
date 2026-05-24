package com.racingdaily.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racingdaily.data.model.ChampionshipSeason
import com.racingdaily.data.model.ChampionshipSubstation
import com.racingdaily.data.model.AppVersion
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChampionshipData(
    val season: ChampionshipSeason? = null,
    val substations: ChampionshipSubstation? = null
)

data class MoreUiState(
    val customData: ChampionshipData = ChampionshipData(),
    val motogpData: ChampionshipData = ChampionshipData(),
    val tcrData: ChampionshipData = ChampionshipData(),
    val appVersion: AppVersion? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MoreViewModel(private val api: ApiService) : ViewModel() {
    private val _state = MutableStateFlow(MoreUiState())
    val state: StateFlow<MoreUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                val customSeason = api.getCustomSeason()
                val customSubs = api.getCustomSubstations()
                val motogpSeason = api.getMotogpSeason()
                val motogpSubs = api.getMotogpSubstations()
                val tcrSeason = api.getTcrSeason()
                val tcrSubs = api.getTcrSubstations()
                val version = api.getAppVersion()
                _state.value = MoreUiState(
                    customData = ChampionshipData(customSeason, customSubs),
                    motogpData = ChampionshipData(motogpSeason, motogpSubs),
                    tcrData = ChampionshipData(tcrSeason, tcrSubs),
                    appVersion = version
                )
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }
}
