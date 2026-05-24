package com.racingdaily.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection

@Composable
actual fun rememberPlayVideo(): (String) -> Unit {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember {
        { url ->
            scope.launch {
                val file = withContext(Dispatchers.IO) {
                    runCatching {
                        val tmp = File(ctx.cacheDir, "video_${url.hashCode()}.mp4")
                        if (!tmp.exists()) {
                            val conn = java.net.URL(url).openConnection() as HttpURLConnection
                            conn.setRequestProperty("Referer", "https://news.romielf.com")
                            conn.setRequestProperty("User-Agent", "RacingDaily/1.2.9")
                            conn.connect()
                            conn.inputStream.use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
                            conn.disconnect()
                        }
                        tmp
                    }.getOrNull()
                }
                file?.let {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(it.absolutePath), "video/mp4")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            }
        }
    }
}
