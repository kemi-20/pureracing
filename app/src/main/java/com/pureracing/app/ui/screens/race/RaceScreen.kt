package com.pureracing.app.ui.screens.race

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pureracing.app.viewmodel.HomeViewModel
import com.pureracing.app.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceScreen(padding: PaddingValues, vm: HomeViewModel = hiltViewModel()) {
    val schedule by vm.schedule.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("赛事") }) },
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
                items(s.data) { race ->
                    ListItem(
                        headlineContent = { Text(race.name) },
                        supportingContent = { Text("${race.circuit} · ${race.raceTime}") },
                        leadingContent = {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    "R${race.round}",
                                    Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
