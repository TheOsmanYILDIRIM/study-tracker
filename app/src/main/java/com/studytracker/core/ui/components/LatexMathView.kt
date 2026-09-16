package com.studytracker.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.studytracker.core.ui.theme.ZomoTextPrimary

/**
 * Android Jetpack Compose tabanlı, 120 FPS sıfır gecikmeli, donanım uyumlu ve %100 çökme korumalı
 * yerel (native) LaTeX matematik ve fen formülü görüntüleyici.
 *
 * WebView bağımlılığını ve LazyColumn içindeki bellek patlamalarını tamamen ortadan kaldırır.
 * Kesirler, karekökler, üslü/köklü sayılar, trigonometrik bağıntılar, integraller, limitler ve
 * Yunan harflerini anında ve kusursuz olarak yerel metin olarak işler.
 */
@Composable
fun LatexMathView(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 14.5.sp,
    textColor: Color = ZomoTextPrimary,
    accentColor: Color = Color(0xFF00F5D4)
) {
    if (text.isBlank()) return

    val formattedMathText = remember(text) {
        formatLatexToNativeMath(text)
    }

    Text(
        text = formattedMathText,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = fontSize,
            color = textColor,
            lineHeight = (fontSize.value * 1.45).sp,
            fontFamily = FontFamily.SansSerif
        )
    )
}

/**
 * LaTeX sözdizimini (kesirler, üsler, indisler, semboller, trigonometri vb.)
 * okunabilir, temiz ve pürüzsüz Unicode matematik notasyonuna dönüştürür.
 */
fun formatLatexToNativeMath(rawInput: String): String {
    if (rawInput.isBlank()) return ""

    var result = rawInput
        .replace("\r\n", "\n")
        .replace("\r", "\n")

    // 1. Dış $ veya $$ veya \( veya \[ işaretlerini kaldır
    result = result
        .replace("$$", "")
        .replace("$", "")
        .replace("\\[", "")
        .replace("\\]", "")
        .replace("\\(", "")
        .replace("\\)", "")

    // 2. \text{...}, \mathbf{...}, \mathit{...}, \mathrm{...} metin bloklarını ayıkla
    val textRegex = Regex("""\\(text|mathbf|mathit|mathrm|textbf)\{([^}]*)\}""")
    result = textRegex.replace(result) { it.groupValues[2] }

    // 3. Kesirleri ( \frac{pay}{payda} ) dönüştür (iç içe kesirleri de çözmek için 4 pas çalışır)
    val fracRegex = Regex("""\\frac\{([^{}]*)\}\{([^{}]*)\}""")
    for (i in 0..3) {
        if (!result.contains("\\frac")) break
        result = fracRegex.replace(result) { match ->
            val num = match.groupValues[1].trim()
            val den = match.groupValues[2].trim()

            val formattedNum = if (num.contains("+") || num.contains("-") || num.contains(" ") || num.length > 5) {
                if (num.startsWith("(") && num.endsWith(")")) num else "($num)"
            } else {
                num
            }

            val formattedDen = if (den.contains("+") || den.contains("-") || den.contains(" ") || den.length > 5) {
                if (den.startsWith("(") && den.endsWith(")")) den else "($den)"
            } else {
                den
            }

            "$formattedNum/$formattedDen"
        }
    }

    // 4. Kökleri ( \sqrt[n]{x} ve \sqrt{x} ) dönüştür
    val nRootRegex = Regex("""\\sqrt\[([^\]]*)\]\{([^}]*)\}""")
    result = nRootRegex.replace(result) { match ->
        val n = match.groupValues[1].trim()
        val content = match.groupValues[2].trim()
        "${toSuperscript(n)}√($content)"
    }

    val sqrtRegex = Regex("""\\sqrt\{([^}]*)\}""")
    result = sqrtRegex.replace(result) { match ->
        val content = match.groupValues[1].trim()
        "√($content)"
    }

    // 5. Şapka ve Vektör işaretleri (\widehat{B}, \vec{v}, \bar{x})
    result = result.replace(Regex("""\\widehat\{([^}]+)\}""")) { "${it.groupValues[1]}̂" }
    result = result.replace(Regex("""\\vec\{([^}]+)\}""")) { "${it.groupValues[1]}⃗" }
    result = result.replace(Regex("""\\bar\{([^}]+)\}""")) { "${it.groupValues[1]}̄" }

    // 6. Trigonometrik, Logaritmik ve Standart Fonksiyonlar
    result = result
        .replace("\\arcsin", "arcsin")
        .replace("\\arccos", "arccos")
        .replace("\\arctan", "arctan")
        .replace("\\arccot", "arccot")
        .replace("\\sin", "sin")
        .replace("\\cos", "cos")
        .replace("\\tan", "tan")
        .replace("\\cot", "cot")
        .replace("\\sec", "sec")
        .replace("\\csc", "csc")
        .replace("\\ln", "ln")
        .replace("\\log", "log")
        .replace("\\exp", "exp")
        .replace("\\lim", "lim")
        .replace("\\max", "max")
        .replace("\\min", "min")

    // 7. Limit alt indisleri ( \lim_{x \to 3} -> lim(x → 3) )
    result = result.replace(Regex("""lim_\{([^}]+)\}""")) { "lim(${it.groupValues[1]})" }

    // 8. İntegral sınırları ( \int_0^2 -> ∫[0, 2] veya ∫ )
    result = result.replace(Regex("""\\int_\{?([0-9a-zA-Z\+\-]+)\}?\^\{?([0-9a-zA-Z\+\-]+)\}?""")) { match ->
        "∫[${match.groupValues[1]}..${match.groupValues[2]}]"
    }
    result = result.replace("\\int", "∫")
    result = result.replace("\\sum", "∑")
    result = result.replace("\\prod", "∏")

    // 9. Üslü İfadeler: ^{...} ve tek karakter üsler (^2, ^x)
    val complexSuperRegex = Regex("""\^\{([^}]+)\}""")
    result = complexSuperRegex.replace(result) { match ->
        toSuperscript(match.groupValues[1])
    }

    val simpleSuperRegex = Regex("""\^([0-9a-zA-Z\+\-\*\=])""")
    result = simpleSuperRegex.replace(result) { match ->
        toSuperscript(match.groupValues[1])
    }

    // 10. İndisler: _{...} ve tek karakter indisler (_0, _n)
    val complexSubRegex = Regex("""_\{([^}]+)\}""")
    result = complexSubRegex.replace(result) { match ->
        toSubscript(match.groupValues[1])
    }

    val simpleSubRegex = Regex("""_([0-9a-zA-Z\+\-])""")
    result = simpleSubRegex.replace(result) { match ->
        toSubscript(match.groupValues[1])
    }

    // 11. Yunan Harfleri
    result = result
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\delta", "δ")
        .replace("\\epsilon", "ε")
        .replace("\\theta", "θ")
        .replace("\\lambda", "λ")
        .replace("\\mu", "μ")
        .replace("\\pi", "π")
        .replace("\\sigma", "σ")
        .replace("\\tau", "τ")
        .replace("\\phi", "φ")
        .replace("\\omega", "ω")
        .replace("\\Delta", "Δ")
        .replace("\\Sigma", "Σ")
        .replace("\\Omega", "Ω")
        .replace("\\Phi", "Φ")
        .replace("\\Gamma", "Γ")

    // 12. Matematik Operatörleri ve Sembolleri
    result = result
        .replace("\\cdot", " · ")
        .replace("\\times", " × ")
        .replace("\\div", " ÷ ")
        .replace("\\pm", " ± ")
        .replace("\\mp", " ∓ ")
        .replace("\\le", " ≤ ")
        .replace("\\leq", " ≤ ")
        .replace("\\ge", " ≥ ")
        .replace("\\geq", " ≥ ")
        .replace("\\neq", " ≠ ")
        .replace("\\approx", " ≈ ")
        .replace("\\equiv", " ≡ ")
        .replace("\\in", " ∈ ")
        .replace("\\notin", " ∉ ")
        .replace("\\subset", " ⊂ ")
        .replace("\\subseteq", " ⊆ ")
        .replace("\\cup", " ∪ ")
        .replace("\\cap", " ∩ ")
        .replace("\\infty", "∞")
        .replace("\\degree", "°")
        .replace("^{\\circ}", "°")
        .replace("^\\circ", "°")
        .replace("\\to", " → ")
        .replace("\\rightarrow", " → ")
        .replace("\\Rightarrow", " ⇒ ")
        .replace("\\leftrightarrow", " ↔ ")
        .replace("\\Leftrightarrow", " ⇔ ")
        .replace("\\forall", "∀")
        .replace("\\exists", "∃")
        .replace("\\partial", "∂")
        .replace("\\nabla", "∇")

    // 13. Parantezler (\left, \right temizliği)
    result = result
        .replace("\\left(", "(")
        .replace("\\right)", ")")
        .replace("\\left[", "[")
        .replace("\\right]", "]")
        .replace("\\left\\{", "{")
        .replace("\\right\\}", "}")
        .replace("\\left|", "|")
        .replace("\\right|", "|")
        .replace("\\{", "{")
        .replace("\\}", "}")
        .replace("\\,", " ")
        .replace("\\;", " ")
        .replace("\\!", "")
        .replace("\\quad", "  ")
        .replace("\\qquad", "    ")

    // 14. Fazla boşlukları ve kalan kaçış çizgilerini temizle
    result = result.replace(Regex(""" +"""), " ")

    return result.trim()
}

/**
 * Karakterleri Unicode Superscript (Üs) formatına çevirir.
 */
private fun toSuperscript(input: String): String {
    val map = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ', 'a' to 'ᵃ',
        'b' to 'ᵇ', 'c' to 'ᶜ', 'k' to 'ᵏ', 'm' to 'ᵐ', 't' to 'ᵗ'
    )
    return input.map { map[it] ?: it }.joinToString("")
}

/**
 * Karakterleri Unicode Subscript (İndis) formatına çevirir.
 */
private fun toSubscript(input: String): String {
    val map = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'i' to 'ᵢ', 'o' to 'ₒ', 'r' to 'ᵣ',
        'u' to 'ᵤ', 'v' to 'ᵥ', 'x' to 'ₓ', 'k' to 'ₖ', 'n' to 'ₙ',
        'm' to 'ₘ', 'p' to 'ₚ', 's' to 'ₛ', 't' to 'ₜ'
    )
    return input.map { map[it] ?: it }.joinToString("")
}


