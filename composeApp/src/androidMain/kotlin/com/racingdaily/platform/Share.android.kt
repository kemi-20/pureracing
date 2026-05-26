package com.racingdaily.platform

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareLauncher(): ShareLauncher {
    val context = LocalContext.current
    return remember(context) {
        ShareLauncher { text ->
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(Intent.createChooser(intent, "Share"))
        }
    }
}
