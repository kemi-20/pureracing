package com.racingdaily.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DetailScreen(articleId: Int, onBack: () -> Unit) {
    var html by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(articleId) {
        scope.launch {
            html = withContext(Dispatchers.IO) {
                runCatching {
                    val client = HttpClient()
                    val raw = client.get("https://news.romielf.com/news.html?id=$articleId").bodyAsText()
                    client.close()
                    raw.replace(Regex("<header[^>]*>.*?</header>", RegexOption.DOT_MATCHES_ALL), "")
                        .replace(Regex("""<script[^>]*>.*?</script>""", RegexOption.DOT_MATCHES_ALL), "")
                        .replace("src=\"./", "src=\"https://news.romielf.com/")
                        .replace("href=\"./", "href=\"https://news.romielf.com/")
                        .replace("<head>", "<head><base href=\"https://news.romielf.com/\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no\"><style>body{font-family:-apple-system,sans-serif;font-size:16px;color:#111;background:#fff;padding:8px;line-height:1.6}img,video{max-width:100%;height:auto}</style>")
                }.getOrNull()
            }
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { TextButton(onBack) { Text("< Back", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp) } }
        if (html == null) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else HtmlView(html!!)
    }
}

@Composable
expect fun HtmlView(html: String)
