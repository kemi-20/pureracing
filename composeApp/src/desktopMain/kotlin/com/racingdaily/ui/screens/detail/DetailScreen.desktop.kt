package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent
import java.awt.Canvas
import javax.swing.SwingUtilities
import androidx.compose.ui.awt.SwingPanel

@Composable
actual fun VideoPlayer(url: String) {
    var playing by remember { mutableStateOf(false) }

    if (playing) {
        SwingPanel(
            factory = {
                val factory = EmbeddedMediaPlayerComponent()
                factory.mediaPlayer().media().play(url, ":http-referrer=https://news.romielf.com")
                factory.mediaPlayer().controls().setRepeat(false)
                factory
            },
            modifier = Modifier.fillMaxWidth().height(240.dp)
        )
    } else {
        Box(
            Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f)).clickable { playing = true },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▶", color = Color.White, fontSize = 36.sp)
                Text("Tap to play video", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}
