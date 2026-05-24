package com.racingdaily.ui.screens.race

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.racingdaily.data.model.RaceGp
import com.racingdaily.data.remote.ApiService

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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("Race Schedule", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        else if (error != null) Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Button({ reloadKey++ }) { Text("Retry") }
            }
        }
        else LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(races) { gp -> ElevatedCard(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) { AsyncImage(gp.gp_logo, null, Modifier.size(48.dp), contentScale = ContentScale.Fit); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(gp.gp_name.ifBlank { gp.race_time_detail }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface); Text(gp.track_name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(gp.race_time_detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; AssistChip({ if (gp.track_id > 0) onTrackClick(gp.track_id) }, { Text(gp.chp_name.ifBlank { "F1" }, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) }) }
                    Spacer(Modifier.height(8.dp)); LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(gp.session.take(6)) { s -> SuggestionChip({ onSessionClick(gp.gp_id.toIntOrNull() ?: 0, s.session_id) }, { Text(s.session_name.firstOrNull() ?: "", fontSize = 10.sp) }) } }
                }
            } }
        }
    }
}
