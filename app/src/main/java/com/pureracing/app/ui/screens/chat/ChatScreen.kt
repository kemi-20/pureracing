package com.pureracing.app.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(padding: PaddingValues) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("社区") }) },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        Box(
            Modifier.fillMaxSize().padding(inner).padding(bottom = padding.calculateBottomPadding()),
            Alignment.Center
        ) {
            Text("社区讨论", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
