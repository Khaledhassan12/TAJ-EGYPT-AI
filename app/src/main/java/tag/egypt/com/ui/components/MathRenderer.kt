package tag.egypt.com.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tag.egypt.com.ui.theme.CyberCyanLight
import tag.egypt.com.ui.theme.Slate800

@Composable
fun DisplayMathCard(
    mathExpression: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Slate800)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatMathSymbols(mathExpression),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = CyberCyanLight,
                fontSize = 17.sp
            )
        )
    }
}

fun formatMathSymbols(input: String): String {
    return input
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\delta", "δ")
        .replace("\\epsilon", "ε")
        .replace("\\theta", "θ")
        .replace("\\lambda", "λ")
        .replace("\\pi", "π")
        .replace("\\sigma", "σ")
        .replace("\\omega", "ω")
        .replace("\\Sigma", "Σ")
        .replace("\\Omega", "Ω")
        .replace("\\infty", "∞")
        .replace("\\approx", "≈")
        .replace("\\neq", "≠")
        .replace("\\leq", "≤")
        .replace("\\geq", "≥")
        .replace("\\pm", "±")
        .replace("\\times", "×")
        .replace("\\div", "÷")
        .replace("\\int", "∫")
        .replace("\\sqrt", "√")
        .replace("^2", "²")
        .replace("^3", "³")
        .replace("^n", "ⁿ")
        .replace("_0", "₀")
        .replace("_1", "₁")
        .replace("_2", "₂")
        .replace("_i", "ᵢ")
        .replace("_n", "ₙ")
        .replace("$$", "")
        .replace("$", "")
        .trim()
}
