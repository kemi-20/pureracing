package com.racingdaily.data.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

@Composable
actual fun rememberReadHistoryController(): ReadHistoryController = remember {
    val preferences = Preferences.userRoot().node("com/racingdaily/pureracing")
    ReadHistoryController(
        initialArticleIds = preferences.get("read_article_ids", null).toReadArticleIds(),
        persist = { ids -> preferences.put("read_article_ids", ids.joinToString(",")) }
    )
}
