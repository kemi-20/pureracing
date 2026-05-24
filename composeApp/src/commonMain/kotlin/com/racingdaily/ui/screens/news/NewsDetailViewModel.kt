package com.racingdaily.ui.screens.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racingdaily.data.model.NewsDetail
import com.racingdaily.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewsDetailUiState(
    val article: NewsDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NewsDetailViewModel(private val api: ApiService, private val articleId: Int) : ViewModel() {
    private val _state = MutableStateFlow(NewsDetailUiState())
    val state: StateFlow<NewsDetailUiState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { api.getNewsDetail(articleId) }
                .onSuccess { _state.value = NewsDetailUiState(article = it) }
                .onFailure { _state.value = NewsDetailUiState(error = it.message) }
        }
    }
}
