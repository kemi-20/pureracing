package com.racingdaily.ui.screens.rankings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racingdaily.data.model.*
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankingUiState(
    val navData: RankingNavData? = null,
    val selectedSeason: RankingOption? = null,
    val selectedChpId: Int = 6,
    val selectedSeasonId: Int = 2026,
    val isDriverTab: Boolean = true,
    val driverData: RankingData? = null,
    val teamData: RankingData? = null,
    val championshipData: ChampionshipSeason? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RankingViewModel(
    private val api: ApiService,
    private val category: String = "f1",
    private val championshipId: Int = 0
) : ViewModel() {
    private val _state = MutableStateFlow(RankingUiState())
    val state: StateFlow<RankingUiState> = _state.asStateFlow()

    init {
        if (category == "f1") {
            loadF1Rankings()
        } else {
            loadChampionshipData()
        }
    }

    fun loadF1Rankings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                val nav = api.getRankingNavigation()
                val driver = api.getDriverRanking(_state.value.selectedChpId, _state.value.selectedSeasonId)
                val team = api.getTeamRanking(_state.value.selectedChpId, _state.value.selectedSeasonId)
                Triple(nav, driver, team)
            }.onSuccess { (nav, driver, team) ->
                // Default to first season option if available
                val seasonOption = nav.list.firstOrNull()?.options?.firstOrNull()
                _state.value = _state.value.copy(
                    navData = nav,
                    driverData = driver,
                    teamData = team,
                    selectedSeason = seasonOption ?: _state.value.selectedSeason,
                    isLoading = false
                )
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    fun selectSeason(chpId: Int, seasonId: Int) {
        _state.value = _state.value.copy(selectedChpId = chpId, selectedSeasonId = seasonId)
        viewModelScope.launch {
            runCatching {
                val driver = api.getDriverRanking(chpId, seasonId)
                val team = api.getTeamRanking(chpId, seasonId)
                Pair(driver, team)
            }.onSuccess { (driver, team) ->
                _state.value = _state.value.copy(driverData = driver, teamData = team)
            }
        }
    }

    fun toggleTab(isDriver: Boolean) {
        _state.value = _state.value.copy(isDriverTab = isDriver)
    }

    private fun loadChampionshipData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                when (category) {
                    "custom" -> api.getCustomDriver(championshipId)
                    "motogp" -> api.getMotogpDriver(championshipId)
                    "tcr" -> api.getTcrDriver(championshipId)
                    else -> api.getCustomDriver(championshipId)
                }
            }.onSuccess {
                _state.value = _state.value.copy(championshipData = it, isLoading = false)
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false, error = it.message)
            }
        }
    }
}
