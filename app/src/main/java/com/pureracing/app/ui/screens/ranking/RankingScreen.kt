package com.pureracing.app.ui.screens.ranking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pureracing.app.data.model.RankItem
import com.pureracing.app.viewmodel.RankViewModel
import com.pureracing.app.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(padding: PaddingValues, vm: RankViewModel = hiltViewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val driverRank by vm.driverRank.collectAsState()
    val constructorRank by vm.constructorRank.collectAsState()

    LaunchedEffect(Unit) { vm.loadRankings(2024) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("排名") })
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("车手") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("车队") })
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        val state = if (selectedTab == 0) driverRank else constructorRank
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(s.message) }
            is UiState.Success -> LazyColumn(
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 8.dp
                )
            ) {
                itemsIndexed(s.data) { _, item -> RankRow(item) }
            }
        }
    }
}

@Composable
private fun RankRow(item: RankItem) {
    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = { Text(item.team) },
        leadingContent = {
            Text(
                "${item.position}",
                style = MaterialTheme.typography.titleLarge,
                color = when (item.position) {
                    1 -> MaterialTheme.colorScheme.secondary
                    2, 3 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text("${item.points}", style = MaterialTheme.typography.titleMedium)
                Text("分", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
    HorizontalDivider()
}
