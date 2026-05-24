package com.racingdaily.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racingdaily.data.model.NavTab
import com.racingdaily.data.model.NewsItem
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val tabs: List<NavTab> = emptyList(),
    val selectedTabId: Int = 1,
    val newsItems: List<NewsItem> = emptyList(),
    val currentPage: Int = 1,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class HomeViewModel(private val api: ApiService) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadTabs()
        loadNews()
    }

    fun loadTabs() {
        viewModelScope.launch {
            runCatching { api.getNavTabs() }
                .onSuccess { data ->
                    _state.value = _state.value.copy(tabs = data.navbar)
                }
        }
    }

    fun selectTab(tabId: Int) {
        _state.value = _state.value.copy(selectedTabId = tabId, newsItems = emptyList(), currentPage = 1)
        loadNews()
    }

    fun loadNews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { api.getNewsList(_state.value.selectedTabId, _state.value.currentPage) }
                .onSuccess { data ->
                    _state.value = _state.value.copy(
                        newsItems = if (_state.value.currentPage == 1) data.list else _state.value.newsItems + data.list,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = it.message
                    )
                }
        }
    }

    fun loadNextPage() {
        _state.value = _state.value.copy(currentPage = _state.value.currentPage + 1)
        loadNews()
    }

    fun refresh() {
        _state.value = _state.value.copy(isRefreshing = true, currentPage = 1)
        loadNews()
    }
}
