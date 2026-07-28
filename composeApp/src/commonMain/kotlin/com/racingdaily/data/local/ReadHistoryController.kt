package com.racingdaily.data.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ReadHistoryController internal constructor(
    initialArticleIds: Set<Int>,
    private val persist: (Set<Int>) -> Unit
) {
    var articleIds by mutableStateOf(initialArticleIds)
        private set

    fun isRead(articleId: Int): Boolean = articleId in articleIds

    fun markRead(articleId: Int) {
        if (articleId <= 0 || articleId in articleIds) return
        articleIds = (articleIds + articleId).takeLast(MAX_READ_ARTICLES).toSet()
        persist(articleIds)
    }

    private companion object {
        const val MAX_READ_ARTICLES = 2_000
    }
}

@Composable
expect fun rememberReadHistoryController(): ReadHistoryController

internal fun String?.toReadArticleIds(): Set<Int> =
    orEmpty()
        .split(',')
        .mapNotNull(String::toIntOrNull)
        .filterTo(linkedSetOf()) { it > 0 }
