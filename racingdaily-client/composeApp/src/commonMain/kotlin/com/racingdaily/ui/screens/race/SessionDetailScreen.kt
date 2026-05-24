package com.racingdaily.ui.screens.race

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.racingdaily.ui.theme.*
import kotlinx.serialization.json.JsonObject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SessionDetailScreen(
    navController: NavHostController,
    gpId: Int,
    sessionId: Int,
    viewModel: SessionDetailViewModel = koinViewModel { parametersOf(gpId, sessionId) }
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
        } else {
            // Navbar tabs
            if (state.navbar.isNotEmpty()) {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Text("Session Results", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                    }
                    itemsIndexed(state.scores) { index, score ->
                        ScoreRow(index + 1, score)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreRow(position: Int, row: JsonObject) {
    val name = row["dr_name"]?.toString()?.removeSurrounding("\"")
        ?: row["driver_name"]?.toString()?.removeSurrounding("\"") ?: ""
    val team = row["team_name"]?.toString()?.removeSurrounding("\"") ?: ""
    val gap = row["gap"]?.toString()?.removeSurrounding("\"") ?: ""
    val teamLogo = row["team_logo"]?.toString()?.removeSurrounding("\"")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (position <= 3) Color.White.copy(alpha = 0.04f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$position",
                color = if (position <= 3) AccentYellow else TextTertiary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp)
            )
            if (teamLogo != null) {
                coil3.compose.AsyncImage(
                    model = teamLogo,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Text(gap, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
