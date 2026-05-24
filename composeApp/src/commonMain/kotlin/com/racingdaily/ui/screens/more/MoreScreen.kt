package com.racingdaily.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.racingdaily.data.model.ChampSub
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.GlassButton
import com.racingdaily.ui.components.GlassChip
import com.racingdaily.ui.components.GlassSurface

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

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("More", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(12.dp))
        }
        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (error != null && customSubs.isEmpty() && motogpSubs.isEmpty() && tcrSubs.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    GlassButton({ reloadKey++ }) { Text("Retry", color = Color.White) }
                }
            }
        }
        item {
            Text(
                "Championships",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
        item { ChampCard("Custom Championship", customSubs) { sub -> onChampClick("custom", sub.custom_id) } }
        item { ChampCard("MotoGP", motogpSubs) { sub -> onChampClick("motogp", sub.motogp_id) } }
        item { ChampCard("TCR", tcrSubs) { sub -> onChampClick("tcr", sub.tcr_id) } }
        item {
            Spacer(Modifier.height(8.dp))
            Text("App", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        }
        item {
            GlassSurface(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                Text("RacingDaily Client v1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun ChampCard(title: String, subs: List<ChampSub>, onClick: (ChampSub) -> Unit) {
    GlassSurface(Modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            subs.take(6).chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { sub ->
                        GlassChip(
                            label = sub.season_name,
                            selected = false,
                            onClick = { onClick(sub) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
