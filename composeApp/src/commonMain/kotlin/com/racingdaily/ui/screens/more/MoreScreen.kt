package com.racingdaily.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.PreferenceGlassRow
import com.racingdaily.ui.components.ScreenHeader

@Composable
@Suppress("UNUSED_PARAMETER")
fun MoreScreen(onChampClick: (String, Int) -> Unit, api: ApiService) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("More", "Series and app info")
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Text("App", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            item {
                PreferenceGlassRow(
                    title = "PureRacing Client",
                    subtitle = "Version 1.0.0",
                    icon = Icons.Rounded.Info,
                    onClick = null
                )
            }
        }
    }
}
