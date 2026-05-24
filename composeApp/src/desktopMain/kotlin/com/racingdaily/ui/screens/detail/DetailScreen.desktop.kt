package com.racingdaily.ui.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

@Composable
actual fun rememberPlayVideo(): (String) -> Unit {
    return remember {
        { url ->
            CoroutineScope(Dispatchers.Default).launch {
                val file = withContext(Dispatchers.IO) {
                    runCatching {
                        val tmp = File(System.getProperty("java.io.tmpdir"), "racingdaily_video_${url.hashCode()}.mp4")
                        if (!tmp.exists()) {
                            val conn = URL(url).openConnection() as HttpURLConnection
                            conn.setRequestProperty("Referer", "https://news.romielf.com")
                            conn.setRequestProperty("User-Agent", "RacingDaily/1.2.9")
                            conn.connect()
                            conn.inputStream.use { input -> tmp.outputStream().use { output -> input.copyTo(output) } }
                            conn.disconnect()
                        }
                        tmp
                    }.getOrNull()
                }
                file?.let {
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(it)
                }
            }
        }
    }
}
