package com.racingdaily.ui.screens.rankings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.racingdaily.data.model.RankingOption
import com.racingdaily.data.model.RankingTab
import com.racingdaily.ui.theme.*
import kotlinx.serialization.json.JsonObject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RankingScreen(
    navController: NavHostController,
    category: String = "f1",
    championshipId: Int = 0,
    viewModel: RankingViewModel = koinViewModel { parametersOf(category, championshipId) }
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Text(
            "Rankings",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading...", color = TextSecondary)
            }
        } else {
            // Driver/Team toggle for F1
            if (category == "f1") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Driver" to true, "Constructor" to false).forEach { (label, isDriver) ->
                        val selected = state.isDriverTab == isDriver
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) AccentRed else Color.White.copy(alpha = 0.05f))
                                .clickable { viewModel.toggleTab(isDriver) }
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(label, color = if (selected) Color.White else TextSecondary, fontSize = 14.sp)
                        }
                    }
                }

                // Season selector
                val navData = state.navData
                if (navData != null) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(navData.list.flatMap { it.options }) { option ->
                            val selected = option.id == state.selectedSeasonId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) AccentBlue.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                    .clickable { viewModel.selectSeason(option.chp_id, option.id) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(option.name, color = if (selected) AccentBlue else TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Ranking data
                val rankingData = if (state.isDriverTab) state.driverData else state.teamData
                RankingDataView(rankingData)
            }
        }
    }
}

@Composable
fun RankingDataView(data: com.racingdaily.data.model.RankingData?) {
    if (data == null) return
    var selectedTab by remember { mutableStateOf(data.list.firstOrNull()?.tab_key ?: "") }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Sub-tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(data.list) { tab ->
                    val selected = tab.tab_key == selectedTab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) AccentRed.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f))
                            .clickable { selectedTab = tab.tab_key }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(tab.tab_name, color = if (selected) AccentRed else TextSecondary, fontSize = 11.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Table rows
        val selectedTabData = data.list.find { it.tab_key == selectedTab }
        if (selectedTabData != null) {
            itemsIndexed(selectedTabData.list) { index, row ->
                RankingRow(index + 1, row)
            }
        }
    }
}

@Composable
fun RankingRow(position: Int, row: JsonObject) {
    // Try common field names for ranking items
    val name = row["driver_abbr_chinese_name"]?.toString()?.removeSurrounding("\"")
        ?: row["team_abbr_chinese_name"]?.toString()?.removeSurrounding("\"")
        ?: row["name"]?.toString()?.removeSurrounding("\"") ?: ""
    val team = row["team_name"]?.toString()?.removeSurrounding("\"") ?: ""
    val score = row["total_score"]?.toString() ?: row["points"]?.toString() ?: ""
    val avatar = row["driver_avatar"]?.toString()?.removeSurrounding("\"")
        ?: row["team_logo"]?.toString()?.removeSurrounding("\"")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (position <= 3) Color.White.copy(alpha = 0.04f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$position",
                color = if (position <= 3) AccentYellow else TextTertiary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )
            if (avatar != null) {
                coil3.compose.AsyncImage(
                    model = avatar,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (team.isNotEmpty()) Text(team, color = TextTertiary, fontSize = 11.sp)
            }
            Text(score, color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
