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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.racingdaily.data.remote.ApiService
import com.racingdaily.ui.components.InfoPill
import com.racingdaily.ui.components.PreferenceGlassRow
import com.racingdaily.ui.components.ScreenHeader
import com.racingdaily.ui.components.SectionLabel

@Composable
@Suppress("UNUSED_PARAMETER")
fun MoreScreen(onChampClick: (String, Int) -> Unit, api: ApiService) {
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("更多", "应用信息")
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                SectionLabel("应用", "纯享赛车安卓与 Windows 客户端")
            }
            item {
                PreferenceGlassRow(
                    title = "纯享赛车",
                    subtitle = "每日赛车新闻与锦标赛数据",
                    icon = Icons.Rounded.Info,
                    onClick = null,
                    endContent = { InfoPill("1.1", accent = MaterialTheme.colorScheme.primary) }
                )
            }
        }
    }
}
