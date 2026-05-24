package com.racingdaily.ui.screens.race

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassSurface

@Composable
fun RaceScreen(onSessionClick: (gpId: Int, sessionId: Int) -> Unit, onTrackClick: (Int) -> Unit, api: ApiService) {
    var races by remember { mutableStateOf<List<RaceGp>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        runCatching { api.getRaceSchedule() }
            .onSuccess { races = it }
            .onFailure { error = it.message ?: "Unable to load race schedule" }
        loading = false
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Race Schedule",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
        )
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ reloadKey++ }) { Text("Retry", color = Color.White) }
                }
            }
            else -> LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(races) { gp ->
                    GlassSurface(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(gp.gp_logo, null, Modifier.size(48.dp), contentScale = ContentScale.Fit)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        gp.gp_name.ifBlank { gp.race_time_detail },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(gp.track_name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(gp.race_time_detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                GlassChip(
                                    label = gp.chp_name.ifBlank { "F1" },
                                    selected = true,
                                    onClick = { if (gp.track_id > 0) onTrackClick(gp.track_id) }
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(gp.session.take(6)) { session ->
                                    GlassChip(
                                        label = session.session_name.firstOrNull() ?: "",
                                        selected = false,
                                        onClick = { onSessionClick(gp.gp_id.toIntOrNull() ?: 0, session.session_id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
