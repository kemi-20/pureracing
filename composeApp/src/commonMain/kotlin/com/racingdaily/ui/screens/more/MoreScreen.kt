package com.racingdaily.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.racingdaily.data.model.ChampionshipSubstation
import com.racingdaily.data.model.SubstationItem
import com.racingdaily.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MoreScreen(navController: NavHostController, viewModel: MoreViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("More", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
        }

        // Championships section
        item { SectionTitle("Championships") }

        item {
            ChampionshipCard("Custom Championship", "Feeder series and custom events",
                onClick = {}) {
                state.customData.substations?.tmp?.take(5)?.forEach { sub ->
                    SubstationChip(sub, "custom") { navController.navigate("championship_driver/custom/${sub.custom_id}") }
                }
            }
        }

        item {
            ChampionshipCard("MotoGP", "Grand Prix motorcycle racing",
                onClick = {}) {
                state.motogpData.substations?.tmp?.take(5)?.forEach { sub ->
                    SubstationChip(sub, "motogp") { navController.navigate("championship_driver/motogp/${sub.motogp_id}") }
                }
            }
        }

        item {
            ChampionshipCard("TCR", "Touring Car Racing",
                onClick = {}) {
                state.tcrData.substations?.tmp?.take(5)?.forEach { sub ->
                    SubstationChip(sub, "tcr") { navController.navigate("championship_driver/tcr/${sub.tcr_id}") }
                }
            }
        }

        // App info
        item { SectionTitle("App") }
        state.appVersion?.let { ver ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(16.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Version", color = TextSecondary, fontSize = 14.sp)
                        Text("v${ver.ver}", color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        color = TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
fun ChampionshipCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    substations: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextTertiary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            substations()
        }
    }
}

@Composable
fun SubstationChip(sub: SubstationItem, category: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(sub.season_name, color = TextSecondary, fontSize = 11.sp)
    }
}
