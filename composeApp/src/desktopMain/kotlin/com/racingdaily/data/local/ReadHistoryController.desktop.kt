package com.racingdaily.data.local

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

@Composable
actual fun rememberReadHistoryController(): ReadHistoryController = remember {
    val preferences = Preferences.userRoot().node("com/racingdaily/pureracing")
    ReadHistoryController(
        initialArticleIds = preferences.readHistoryValue().toReadArticleIds(),
        persist = { ids -> preferences.writeHistoryValue(ids.joinToString(",")) }
    )
}

private fun Preferences.readHistoryValue(): String? {
    val chunkCount = getInt(ReadHistoryChunkCountKey, 0)
    if (chunkCount !in 1..MaxReadHistoryChunks) return get(LegacyReadHistoryKey, null)
    return buildString {
        repeat(chunkCount) { index -> append(get("$ReadHistoryChunkPrefix$index", "")) }
    }
}

private fun Preferences.writeHistoryValue(value: String) {
    val oldChunkCount = getInt(ReadHistoryChunkCountKey, 0)
    val chunks = value.chunked(ReadHistoryChunkSize).ifEmpty { listOf("") }
    chunks.forEachIndexed { index, chunk -> put("$ReadHistoryChunkPrefix$index", chunk) }
    putInt(ReadHistoryChunkCountKey, chunks.size)
    for (index in chunks.size until oldChunkCount) remove("$ReadHistoryChunkPrefix$index")
    remove(LegacyReadHistoryKey)
}

private const val LegacyReadHistoryKey = "read_article_ids"
private const val ReadHistoryChunkCountKey = "read_article_ids_chunk_count"
private const val ReadHistoryChunkPrefix = "read_article_ids_chunk_"
private const val ReadHistoryChunkSize = 7_000
private const val MaxReadHistoryChunks = 16
