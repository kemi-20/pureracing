package com.pureracing.app.ui.screens.track

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(padding: PaddingValues) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("赛道") }) },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        Box(
            Modifier.fillMaxSize().padding(inner).padding(bottom = padding.calculateBottomPadding()),
            Alignment.Center
        ) {
            Text("赛道信息", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
