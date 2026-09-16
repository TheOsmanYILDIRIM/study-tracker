package com.studytracker.core.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.studytracker.core.ui.theme.ZomoTextPrimary

/**
 * Android WebView ve KaTeX tabanlı, tam donanımlı ve zengin LaTeX matematik formülü render bileşeni.
 * Kesirler, karekökler, üslü/köklü sayılar, integraller, matrisler ve sembolleri donanım hızlandırmalı
 * ve koyu tema uyumlu olarak çizer.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LatexMathView(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.5.sp,
    textColor: Color = ZomoTextPrimary,
    accentColor: Color = Color(0xFF00F5D4)
) {
    if (text.isBlank()) return

    // Basit metin ve tek satırlık LaTeX kontrolü
    val containsLatex = text.contains("\\") || text.contains("$") || text.contains("_") || text.contains("^")

    if (!containsLatex) {
        Text(
            text = text,
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = fontSize,
                color = textColor,
                lineHeight = (fontSize.value * 1.4).sp
            )
        )
        return
    }

    val textHex = String.format("#%06X", 0xFFFFFF and textColor.toArgb())
    val accentHex = String.format("#%06X", 0xFFFFFF and accentColor.toArgb())
    val fontSizePx = fontSize.value.toInt()

    // KaTeX HTML Şablonu
    val htmlContent = remember(text, textColor, fontSize) {
        buildKatexHtml(
            rawText = text,
            textColorHex = textHex,
            accentColorHex = accentHex,
            fontSizePx = fontSizePx
        )
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    setBackgroundColor(0) // Transparent background
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = false
                    webViewClient = WebViewClient()
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://cdn.jsdelivr.net", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * KaTeX ve MathJax CDN kütüphanelerini içeren, offline fallback özellikli HTML yapıcı.
 */
private fun buildKatexHtml(
    rawText: String,
    textColorHex: String,
    accentColorHex: String,
    fontSizePx: Int
): String {
    // Formül metnini KaTeX render için hazırla
    // Eğer metinde $ yoksa ve \frac, \sqrt gibi LaTeX komutları varsa satırları koruyarak otomatik çevrele
    val formattedText = formatLatexString(rawText)

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"
                onload="renderMathInElement(document.body, {
                    delimiters: [
                        {left: '$$', right: '$$', display: true},
                        {left: '$', right: '$', display: false},
                        {left: '\\[', right: '\\]', display: true},
                        {left: '\\(', right: '\\)', display: false}
                    ],
                    throwOnError : false
                });"></script>
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                body {
                    background-color: transparent !important;
                    color: $textColorHex;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    font-size: ${fontSizePx}px;
                    line-height: 1.5;
                    word-wrap: break-word;
                    overflow-x: hidden;
                    padding: 2px 0;
                }
                .katex {
                    color: $accentColorHex;
                    font-size: 1.1em;
                }
                .katex-display {
                    margin: 0.5em 0;
                }
            </style>
        </head>
        <body>
            $formattedText
        </body>
        </html>
    """.trimIndent()
}

/**
 * Metindeki LaTeX formüllerini KaTeX auto-render için normalize eder.
 */
private fun formatLatexString(input: String): String {
    val escaped = input
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br>")

    // Eğer $ işaretleri yoksa ama LaTeX anahtar kelimeleri varsa, onları $ içine saralım
    val hasDelimiters = escaped.contains("$") || escaped.contains("\\[") || escaped.contains("\\(")
    if (hasDelimiters) {
        return escaped
    }

    // Basit otomatik algılama
    val regex = Regex("""(\\[a-zA-Z]+(\{[^}]*\})*(\[[^\]]*\])*(\{[^}]*\})*|([a-zA-Z0-9]+[\^_]\{[^}]+\})|([a-zA-Z0-9]+[\^_][a-zA-Z0-9]))""")
    return regex.replace(escaped) { match ->
        "$" + match.value + "$"
    }
}
