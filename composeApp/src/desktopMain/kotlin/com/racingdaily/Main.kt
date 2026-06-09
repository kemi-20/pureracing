package com.racingdaily

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.util.Locale

fun main() = application {
    val appName = if (Locale.getDefault().language == "zh") "纯享赛车" else "PureRacing"
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        state = rememberWindowState(width = 480.dp, height = 900.dp),
        icon = painterResource("app_icon.png"),
    ) {
        RacingDailyClient()
    }
}
