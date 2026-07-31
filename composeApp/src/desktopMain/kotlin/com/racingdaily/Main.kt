package com.racingdaily

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.racingdaily.resources.Res
import com.racingdaily.resources.app_icon
import java.util.Locale
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    val appName = if (Locale.getDefault().language == "zh") "纯享赛车" else "PureRacing"
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        state = rememberWindowState(width = 480.dp, height = 900.dp),
        icon = painterResource(Res.drawable.app_icon),
    ) {
        RacingDailyClient()
    }
}
