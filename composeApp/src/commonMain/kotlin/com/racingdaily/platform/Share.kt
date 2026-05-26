package com.racingdaily.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberShareLauncher(): ShareLauncher

fun interface ShareLauncher {
    fun share(text: String)
}
