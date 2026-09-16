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
 * KaTeX CDN kütüphanelerini içeren ve formülleri güvenle işleyen HTML yapıcı.
 */
private fun buildKatexHtml(
    rawText: String,
    textColorHex: String,
    accentColorHex: String,
    fontSizePx: Int
): String {
    val formattedText = formatLatexString(rawText)

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
            <script defer src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>
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
                    line-height: 1.55;
                    word-wrap: break-word;
                    overflow-x: hidden;
                    padding: 2px 0;
                }
                .katex {
                    color: $accentColorHex;
                    font-size: 1.15em;
                }
                .katex-display {
                    margin: 0.5em 0;
                    overflow-x: auto;
                    overflow-y: hidden;
                }
                .katex .base {
                    margin-top: 2px;
                    margin-bottom: 2px;
                }
            </style>
        </head>
        <body>
            <div id="content">$formattedText</div>
            <script>
                function renderKatex() {
                    if (window.renderMathInElement) {
                        renderMathInElement(document.getElementById('content') || document.body, {
                            delimiters: [
                                {left: '$$', right: '$$', display: true},
                                {left: '$', right: '$', display: false},
                                {left: '\\[', right: '\\]', display: true},
                                {left: '\\(', right: '\\)', display: false}
                            ],
                            throwOnError : false,
                            errorColor: '$accentColorHex'
                        });
                    } else {
                        setTimeout(renderKatex, 50);
                    }
                }
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', renderKatex);
                } else {
                    renderKatex();
                }
                window.addEventListener('load', renderKatex);
            </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Metindeki LaTeX formüllerini parçalamadan ve parantez bütünlüğünü bozmadan
 * KaTeX için $...$ veya $$...$$ içine sarar.
 */
internal fun formatLatexString(input: String): String {
    // 1. Eğer metin zaten $ veya $$ veya \( veya \[ içeriyorsa, sadece satır sonlarını ve html etiketlerini escape et
    val hasExplicitDelimiters = input.contains("$") || input.contains("\\[") || input.contains("\\(")
    if (hasExplicitDelimiters) {
        return escapeHtmlExceptDelimiters(input)
    }

    // 2. Satır satır inceleyip matematik bloklarını bütün olarak yakala
    val lines = input.lines()
    val processedLines = lines.map { line ->
        processSingleLineLatex(line)
    }

    return processedLines.joinToString("<br>")
}

/**
 * Tek bir satırı analiz ederek saf formül satırlarını veya metin içi LaTeX formüllerini sarar.
 */
private fun processSingleLineLatex(line: String): String {
    val trimmed = line.trim()
    if (trimmed.isEmpty()) return ""

    val containsLatexCommands = trimmed.contains("\\") || trimmed.contains("^") || trimmed.contains("_")
    if (!containsLatexCommands) {
        return escapeHtml(trimmed)
    }

    // Türkçe yaygın kelimeler veya uzun Türkçe metin kontrolü
    val turkishProseRegex = Regex("""(?i)\b(olmak|üzere|ifadesinin|değeri|kaçtır|hangisidir|eşiti|hali|aşağıdakilerden|olduğuna|göre|elde|edilir|bulunur|ve|için|ile|noktasındaki|fonksiyonunun|denklemini|sağlayan|değerlerinin|toplamı|üçgeninde|kenar|uzunlukları|seçenek|kökler|köklerin|yazılarak|düzenlenirse|çarpanlarına|ayrılırsa|bağıntıları|uygulanırsa|teoremine|paydalar|eşitlenirse|radyan|radyandır|toplanırsa|farkı|oranı)\b|[çğıöşüÇĞİÖŞÜ]""")

    val hasProse = turkishProseRegex.containsMatchIn(trimmed)

    // Eğer satırda hiç Türkçe kelime yoksa ve LaTeX komutu varsa, tüm satırı tek bir bütünleşik formül olarak sar!
    if (!hasProse) {
        return "$$" + trimmed + "$$"
    }

    // Satırda hem Türkçe metin hem LaTeX formülü varsa:
    // Formül parçalarını (iç içe \left( ... \right), \frac{...}{...}, \cos, \sin, değişkenler vb.) bütün olarak yakala
    return wrapInlineMathExpressions(trimmed)
}

/**
 * Cümle içindeki matematiksel ifadeleri (\\ ile başlayan bloklar ve parametreleri) bütünleşik olarak $...$ içine alır.
 */
private fun wrapInlineMathExpressions(text: String): String {
    // 1. \ ile başlayan veya x \in ..., a = 5\text{ cm} gibi matematiksel kümeleri yakalayan regex
    // Parantez ve argüman zincirlerini (\left(...\right), \frac{...}{...}, \sqrt{...}) tek parça tutar
    val mathPattern = Regex("""(?<!\$)(\\?[a-zA-Z0-9]+(\s*[\^_]\s*(\{[^}]+\}|[a-zA-Z0-9]))*(\s*[\+\-\*\/\=\<\>\:\cdot\in\Rightarrow]\s*(\\?[a-zA-Z0-9]+(\{[^}]*\})*(\[[^\]]*\])*(\([^)]*\))*))*|\\(frac|sqrt|sin|cos|tan|cot|sec|csc|arcsin|arccos|arctan|left|right|int|sum|prod|lim|pi|alpha|beta|theta|cdot|in|widehat|text|mathbf|times|pm|mp|le|ge|neq|approx|infty|to|rightarrow|Rightarrow)(\{[^}]*\}|\[[^\]]*\]|\([^\)]*\)|\s*[a-zA-Z0-9\+\-\*\/\=\(\)\,\.\^]+)*)(?!\$)""")

    val result = StringBuilder()
    var lastIndex = 0

    // Daha güvenli ve temiz yaklaşım: \ ile başlayan tüm matematik bloklarını ve ilişkili terimlerini bul
    val latexChunkRegex = Regex("""(\\[a-zA-Z]+(\{[^}]*\}|\[[^\]]*\]|\([^\)]*\)|\s*)*([0-9a-zA-Z\+\-\*\/\=\(\)\,\.\^\_]|(\\[a-zA-Z]+(\{[^}]*\}|\[[^\]]*\]|\([^\)]*\))*))*)""")

    val matches = latexChunkRegex.findAll(text).toList()

    if (matches.isEmpty()) {
        return escapeHtml(text)
    }

    for (match in matches) {
        // Öncesindeki metin
        if (match.range.first > lastIndex) {
            val prefix = text.substring(lastIndex, match.range.first)
            result.append(escapeHtml(prefix))
        }

        val mathSnippet = match.value.trim()
        if (mathSnippet.isNotEmpty()) {
            result.append("$").append(mathSnippet).append("$")
        }

        lastIndex = match.range.last + 1
    }

    if (lastIndex < text.length) {
        val suffix = text.substring(lastIndex)
        result.append(escapeHtml(suffix))
    }

    return result.toString()
}

private fun escapeHtml(text: String): String {
    return text
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

private fun escapeHtmlExceptDelimiters(text: String): String {
    return text
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\n", "<br>")
}

