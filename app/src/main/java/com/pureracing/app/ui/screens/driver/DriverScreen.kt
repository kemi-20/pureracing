package com.pureracing.app.ui.screens.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pureracing.app.viewmodel.RankViewModel
import com.pureracing.app.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverScreen(padding: PaddingValues, vm: RankViewModel = hiltViewModel()) {
    val driverRank by vm.driverRank.collectAsState()

    LaunchedEffect(Unit) { vm.loadRankings(2024) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("车手") }) },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        when (val s = driverRank) {
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
                items(s.data) { driver ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${driver.position}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(40.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(driver.name, style = MaterialTheme.typography.titleMedium)
                                Text(driver.team, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(driver.nationality, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${driver.points}", style = MaterialTheme.typography.titleLarge)
                                Text("分", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
