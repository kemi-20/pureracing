package com.racingdaily

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PureRacing",
        state = rememberWindowState(width = 480.dp, height = 900.dp),
        icon = painterResource("app_icon.png"),
    ) {
        RacingDailyClient()
    }
}
