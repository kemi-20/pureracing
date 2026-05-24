package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetailScreen(articleId: Int, onBack: () -> Unit) {
    OpenUrl("https://news.romielf.com/news.html?id=$articleId")
    LaunchedEffect(Unit) { onBack() }
}

@Composable
expect fun OpenUrl(url: String)
