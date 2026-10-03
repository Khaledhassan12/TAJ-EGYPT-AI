package tag.egypt.com.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tag.egypt.com.model.ChatMessage
import tag.egypt.com.ui.theme.CyberCyan
import tag.egypt.com.ui.theme.EmeraldGreen
import tag.egypt.com.ui.theme.RoseRed
import tag.egypt.com.ui.theme.Slate400
import tag.egypt.com.ui.theme.Slate800
import tag.egypt.com.ui.theme.Slate900

/**
 * Expandable Material 3 AI Activity & Telemetry card in TAJ EGY.
 */
@Composable
fun AiActivityCard(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "card_arrow")

    val statusColor = when (message.status) {
        ChatMessage.Status.STREAMING -> CyberCyan
        ChatMessage.Status.COMPLETED -> EmeraldGreen
        ChatMessage.Status.ERROR -> RoseRed
        else -> Slate400
    }

    val statusText = when (message.status) {
        ChatMessage.Status.STREAMING -> "Generating…"
        ChatMessage.Status.COMPLETED -> "Completed"
        ChatMessage.Status.ERROR -> "Error"
        else -> "Ready"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    )
                    if (message.modelUsed.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•  ${message.modelUsed}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                    }
                    if (message.latencyMs > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${String.format(java.util.Locale.US, "%.2fs", message.latencyMs / 1000.0)})",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand activity telemetry",
                    tint = Slate400,
                    modifier = Modifier.size(18.dp).rotate(rotation)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Slate800)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TelemetryRow("Provider", message.providerUsed.ifEmpty { "Default" })
                    TelemetryRow("Model", message.modelUsed.ifEmpty { "Default" })
                    if (message.latencyMs > 0) {
                        TelemetryRow("Round-trip Time", String.format(java.util.Locale.US, "%.2f sec", message.latencyMs / 1000.0))
                    }
                    if (message.inputTokens > 0 || message.outputTokens > 0) {
                        TelemetryRow("Input Tokens", String.format(java.util.Locale.US, "%,d", message.inputTokens))
                        TelemetryRow("Output Tokens", String.format(java.util.Locale.US, "%,d", message.outputTokens))
                        TelemetryRow("Total Tokens", String.format(java.util.Locale.US, "%,d", message.totalTokens))
                    }
                    if (message.estimatedCost > 0) {
                        TelemetryRow("Estimated Cost", String.format(java.util.Locale.US, "$%.5f", message.estimatedCost))
                    }
                    if (message.reasoningSummary.isNotEmpty()) {
                        TelemetryRow("Reasoning / Skill", message.reasoningSummary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
