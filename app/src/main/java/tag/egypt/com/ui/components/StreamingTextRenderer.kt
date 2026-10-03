package tag.egypt.com.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import tag.egypt.com.ui.theme.CyberCyan

enum class StreamingAnimationMode {
    TYPING,
    FADE,
    SCALE_IN,
    JUMP,
    NONE
}

@Composable
fun StreamingTextRenderer(
    text: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    animationMode: StreamingAnimationMode = StreamingAnimationMode.TYPING
) {
    if (text.isEmpty() && isStreaming) {
        StreamingPulseIndicator(modifier = modifier)
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Bottom
        ) {
            when (animationMode) {
                StreamingAnimationMode.FADE -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(150))
                    ) {
                        Text(text = text, style = style, color = color)
                    }
                }
                StreamingAnimationMode.SCALE_IN -> {
                    AnimatedVisibility(
                        visible = true,
                        enter = scaleIn(animationSpec = tween(150, easing = FastOutSlowInEasing))
                    ) {
                        Text(text = text, style = style, color = color)
                    }
                }
                else -> {
                    Text(text = text, style = style, color = color)
                }
            }

            if (isStreaming) {
                Spacer(modifier = Modifier.width(3.dp))
                StreamingCursor()
            }
        }
    }
}

@Composable
fun StreamingCursor(
    modifier: Modifier = Modifier,
    color: Color = CyberCyan
) {
    val transition = rememberInfiniteTransition(label = "cursor_blink")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    Box(
        modifier = modifier
            .width(7.dp)
            .height(18.dp)
            .alpha(alpha)
            .background(color = color, shape = CircleShape)
    )
}

@Composable
fun StreamingPulseIndicator(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha1 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 0), RepeatMode.Reverse),
        label = "a1"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 150), RepeatMode.Reverse),
        label = "a2"
    )
    val alpha3 by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 300), RepeatMode.Reverse),
        label = "a3"
    )

    Row(
        modifier = modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).alpha(alpha1).background(MaterialTheme.colorScheme.primary))
        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).alpha(alpha2).background(MaterialTheme.colorScheme.primary))
        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).alpha(alpha3).background(MaterialTheme.colorScheme.primary))
    }
}
