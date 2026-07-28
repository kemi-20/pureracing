package com.racingdaily.data.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberReadHistoryController(): ReadHistoryController {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        val preferences = context.getSharedPreferences("pureracing_preferences", 0)
        ReadHistoryController(
            initialArticleIds = preferences.getString("read_article_ids", null).toReadArticleIds(),
            persist = { ids ->
                preferences.edit().putString("read_article_ids", ids.joinToString(",")).apply()
            }
        )
    }
}
