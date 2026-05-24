package com.racingdaily.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.racingdaily.data.model.ChampSub
import com.racingdaily.data.remote.ApiService

data class ChampData(val subs: List<ChampSub> = emptyList())

@Composable
fun MoreScreen(onChampClick: (String, Int) -> Unit, api: ApiService) {
    var customSubs by remember { mutableStateOf<List<ChampSub>>(emptyList()) }
    var motogpSubs by remember { mutableStateOf<List<ChampSub>>(emptyList()) }
    var tcrSubs by remember { mutableStateOf<List<ChampSub>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(reloadKey) {
        loading = true
        error = null
        runCatching { api.getCustomSubstation().tmp }.onSuccess { customSubs = it }.onFailure { error = it.message }
        runCatching { api.getMotogpSubstation().tmp }.onSuccess { motogpSubs = it }.onFailure { error = error ?: it.message }
        runCatching { api.getTcrSubstation().tmp }.onSuccess { tcrSubs = it }.onFailure { error = error ?: it.message }
        loading = false
    }

    LazyColumn(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("More", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground); Spacer(Modifier.height(12.dp)) }
        if (loading) {
            item { Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = androidx.compose.ui.Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
        } else if (error != null && customSubs.isEmpty() && motogpSubs.isEmpty() && tcrSubs.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Button({ reloadKey++ }) { Text("Retry") }
                }
            }
        }
        item { Text("Championships", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold) }
        item { ChampCard("Custom Championship", customSubs) { sub -> onChampClick("custom", sub.custom_id) } }
        item { ChampCard("MotoGP", motogpSubs) { sub -> onChampClick("motogp", sub.motogp_id) } }
        item { ChampCard("TCR", tcrSubs) { sub -> onChampClick("tcr", sub.tcr_id) } }
        item { Spacer(Modifier.height(8.dp)); Text("App", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold) }
        item { ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) { Text("RacingDaily Client v1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(16.dp)) } }
    }
}

@Composable
fun ChampCard(title: String, subs: List<ChampSub>, onClick: (ChampSub) -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) { Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium); Spacer(Modifier.height(6.dp))
            subs.take(6).chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { sub -> SuggestionChip({ onClick(sub) }, { Text(sub.season_name, fontSize = 11.sp) }) } }; Spacer(Modifier.height(4.dp)) } }
    }
}
