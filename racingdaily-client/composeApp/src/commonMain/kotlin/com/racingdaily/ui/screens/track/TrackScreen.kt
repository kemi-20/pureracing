package com.racingdaily.ui.screens.track

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.racingdaily.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TrackScreen(
    navController: NavHostController,
    trackId: Int,
    viewModel: TrackViewModel = koinViewModel { parametersOf(trackId) }
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text("< Back", color = AccentBlue, fontSize = 14.sp, modifier = Modifier.clickable { navController.popBackStack() })
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...", color = TextSecondary)
            }
        } else if (state.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error}", color = AccentRed)
            }
        }

        state.trackInfo?.let { data ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
            ) {
                val track = data.track
                Text(track.chinese_name.ifEmpty { track.name }, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("${track.country} - ${track.location}", color = TextSecondary, fontSize = 14.sp)

                if (track.map_img.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    AsyncImage(
                        model = track.map_img,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text("Coordinates", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("${track.longitude}, ${track.latitude}", color = TextPrimary, fontSize = 14.sp)

                // Score/history if available
                state.trackScore?.let { score ->
                    Spacer(Modifier.height(16.dp))
                    Text("Race History", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    score.history.take(5).forEach { race ->
                        val gpName = race["gp_name"]?.toString()?.removeSurrounding("\"") ?: ""
                        val driver = race["driver_name"]?.toString()?.removeSurrounding("\"") ?: ""
                        if (gpName.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(gpName, color = TextPrimary, fontSize = 13.sp)
                                if (driver.isNotEmpty()) Text(driver, color = TextTertiary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
