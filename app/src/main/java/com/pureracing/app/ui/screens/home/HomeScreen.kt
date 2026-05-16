package com.pureracing.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pureracing.app.data.model.RaceSchedule
import com.pureracing.app.viewmodel.HomeViewModel
import com.pureracing.app.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(padding: PaddingValues, vm: HomeViewModel = hiltViewModel()) {
    val seasons by vm.seasons.collectAsState()
    val schedule by vm.schedule.collectAsState()

    LaunchedEffect(seasons) {
        if (seasons is UiState.Success) {
            val list = (seasons as UiState.Success).data
            if (list.isNotEmpty()) vm.loadSchedule(list.first().id)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("纯享赛车") }) },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        when (val s = schedule) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message) }
            is UiState.Success -> LazyColumn(
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp,
                    start = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(s.data) { race -> RaceScheduleCard(race) }
            }
        }
    }
}

@Composable
private fun RaceScheduleCard(race: RaceSchedule) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("第${race.round}站", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                StatusChip(race.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(race.name, style = MaterialTheme.typography.titleMedium)
            Text(race.circuit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(race.raceTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusChip(status: Int) {
    val (label, color) = when (status) {
        1 -> "进行中" to MaterialTheme.colorScheme.primary
        2 -> "已结束" to MaterialTheme.colorScheme.onSurfaceVariant
        else -> "未开始" to MaterialTheme.colorScheme.secondary
    }
    Surface(color = color.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small) {
        Text(label, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}
