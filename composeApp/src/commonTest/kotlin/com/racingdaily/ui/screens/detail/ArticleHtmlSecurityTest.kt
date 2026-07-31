package com.racingdaily.ui.screens.detail

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArticleHtmlSecurityTest {
    @Test
    fun onlyBundledScriptsReceiveTheContentSecurityNonce() {
        val document = buildArticleHtmlDocument(
            html = "<p>Article</p><script>alert('untrusted')</script>",
            darkTheme = false,
            playerScript = "window.playerLoaded = true;",
            playerTemplate = "<style>:host { color: white; }</style>"
        )

        assertTrue(document.contains("script-src 'nonce-pureracing'"))
        assertEquals(2, Regex("<script nonce=\"pureracing\"").findAll(document).count())
        assertTrue(document.contains("<script>alert('untrusted')</script>"))
    }
}
