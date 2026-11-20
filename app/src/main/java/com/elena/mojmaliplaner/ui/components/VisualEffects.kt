package com.elena.mojmaliplaner.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import com.elena.mojmaliplaner.ui.theme.SoftGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val originXFraction: Float,
    val originYFraction: Float,
    val velocityX: Float,
    val velocityY: Float,
    val gravity: Float,
    val size: Float,
    val color: Color,
    val initialRotation: Float,
    val rotationSpeed: Float,
    val shapeType: Int, // 0 = ribbon, 1 = circle, 2 = 4-point star
    val wobbleFreq: Float,
    val wobbleAmp: Float
)

private val ConfettiPalette = listOf(
    SoftGold,
    Color(0xFFFFD54F),
    PastelBlue,
    PastelPurple,
    Color(0xFFFF8A80),
    Color(0xFF80CBC4),
    Color(0xFFF48FB1),
    Color(0xFFA5D6A7)
)

/**
 * Full-bleed physics-driven confetti burst drawn smoothly on Compose Canvas.
 * Re-triggers automatically whenever [triggerKey] changes.
 */
@Composable
fun ConfettiBurstOverlay(
    triggerKey: Any,
    modifier: Modifier = Modifier,
    particleCount: Int = 72,
    durationSeconds: Float = 2.8f,
    originYFraction: Float = 0.36f
) {
    val particles = remember(triggerKey, particleCount, originYFraction) {
        val rng = Random(triggerKey.hashCode().toLong() xor System.nanoTime())
        List(particleCount) { idx ->
            val angle = rng.nextFloat() * (2f * PI.toFloat())
            val speed = 180f + rng.nextFloat() * 760f
            // Bias slightly upward for a celebratory fountain burst
            val vx = cos(angle) * speed * (0.7f + rng.nextFloat() * 0.5f)
            val vy = sin(angle) * speed - (260f + rng.nextFloat() * 340f)
            ConfettiParticle(
                originXFraction = 0.5f + (rng.nextFloat() - 0.5f) * 0.14f,
                originYFraction = originYFraction + (rng.nextFloat() - 0.5f) * 0.06f,
                velocityX = vx,
                velocityY = vy,
                gravity = 640f + rng.nextFloat() * 420f,
                size = 7f + rng.nextFloat() * 11f,
                color = ConfettiPalette[idx % ConfettiPalette.size],
                initialRotation = rng.nextFloat() * 360f,
                rotationSpeed = (rng.nextFloat() - 0.5f) * 520f,
                shapeType = idx % 3,
                wobbleFreq = 3f + rng.nextFloat() * 5f,
                wobbleAmp = 10f + rng.nextFloat() * 24f
            )
        }
    }

    var elapsedSeconds by remember(triggerKey) { mutableFloatStateOf(0f) }

    LaunchedEffect(triggerKey) {
        elapsedSeconds = 0f
        var startNanos = 0L
        while (elapsedSeconds < durationSeconds) {
            withFrameNanos { frameNanos ->
                if (startNanos == 0L) startNanos = frameNanos
                elapsedSeconds = (frameNanos - startNanos) / 1_000_000_000f
            }
        }
    }

    if (elapsedSeconds >= durationSeconds) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = elapsedSeconds
        val fadeStart = durationSeconds * 0.65f
        val globalAlpha = if (t > fadeStart) {
            (1f - (t - fadeStart) / (durationSeconds - fadeStart)).coerceIn(0f, 1f)
        } else {
            1f
        }

        particles.forEach { p ->
            // Gentle air drag on horizontal motion
            val drag = 1f / (1f + 0.85f * t)
            val x = size.width * p.originXFraction +
                p.velocityX * t * drag +
                sin(t * p.wobbleFreq + p.initialRotation) * p.wobbleAmp
            val y = size.height * p.originYFraction +
                p.velocityY * t +
                0.5f * p.gravity * t * t

            if (y > size.height + 40f || x < -60f || x > size.width + 60f) return@forEach

            val rot = p.initialRotation + p.rotationSpeed * t
            val tumble = (0.35f + 0.65f * kotlin.math.abs(cos(t * 6f + p.initialRotation)))
            val colorWithAlpha = p.color.copy(alpha = globalAlpha)

            rotate(degrees = rot, pivot = Offset(x, y)) {
                when (p.shapeType) {
                    0 -> {
                        drawRoundRect(
                            color = colorWithAlpha,
                            topLeft = Offset(x - p.size * 0.65f, y - p.size * 0.35f * tumble),
                            size = Size(p.size * 1.3f, p.size * 0.7f * tumble),
                            cornerRadius = CornerRadius(3f, 3f)
                        )
                    }
                    1 -> {
                        drawCircle(
                            color = colorWithAlpha,
                            radius = p.size * 0.45f,
                            center = Offset(x, y)
                        )
                    }
                    else -> {
                        drawSparkleStar(
                            center = Offset(x, y),
                            radius = p.size * 0.65f,
                            color = colorWithAlpha
                        )
                    }
                }
            }
        }
    }
}

internal fun DrawScope.drawSparkleStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val inner = radius * 0.36f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + inner, center.y - inner)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + inner, center.y + inner)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - inner, center.y + inner)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - inner, center.y - inner)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * Softly rotating sunburst rays behind celebration stars & unlocked stickers.
 */
@Composable
fun SunburstGlow(
    color: Color,
    modifier: Modifier = Modifier,
    rayCount: Int = 12
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunburst")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst_angle"
    )

    Canvas(modifier = modifier) {
        val radius = size.minDimension * 0.5f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Soft center radial glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.35f),
                    color.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Rotating wedge rays
        rotate(degrees = angle, pivot = center) {
            val stepDeg = 360f / rayCount
            val wedgeHalfRad = (stepDeg * 0.24f) * (PI.toFloat() / 180f)
            for (i in 0 until rayCount) {
                val baseRad = (i * stepDeg) * (PI.toFloat() / 180f)
                val p1 = Offset(
                    center.x + cos(baseRad - wedgeHalfRad) * radius,
                    center.y + sin(baseRad - wedgeHalfRad) * radius
                )
                val p2 = Offset(
                    center.x + cos(baseRad + wedgeHalfRad) * radius,
                    center.y + sin(baseRad + wedgeHalfRad) * radius
                )
                val rayPath = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    close()
                }
                drawPath(
                    path = rayPath,
                    brush = Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.28f), Color.Transparent),
                        center = center,
                        radius = radius
                    )
                )
            }
        }
    }
}

/**
 * Cozy drifting pastel gradient orbs providing storybook depth behind screens.
 */
@Composable
fun AnimatedAtmosphereBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "atmosphere")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_drift"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val alphaFactor = if (isDarkTheme) 0.14f else 0.18f

        val orb1Center = Offset(
            x = size.width * (0.18f + 0.08f * drift),
            y = size.height * (0.14f + 0.05f * (1f - drift))
        )
        val orb2Center = Offset(
            x = size.width * (0.86f - 0.08f * drift),
            y = size.height * (0.44f + 0.06f * drift)
        )
        val orb3Center = Offset(
            x = size.width * (0.28f + 0.10f * (1f - drift)),
            y = size.height * (0.82f - 0.05f * drift)
        )

        val orbRadius = size.minDimension * 0.52f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PastelBlue.copy(alpha = alphaFactor), Color.Transparent),
                center = orb1Center,
                radius = orbRadius
            ),
            radius = orbRadius,
            center = orb1Center
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(PastelPurple.copy(alpha = alphaFactor), Color.Transparent),
                center = orb2Center,
                radius = orbRadius * 1.05f
            ),
            radius = orbRadius * 1.05f,
            center = orb2Center
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SoftGold.copy(alpha = alphaFactor * 0.9f), Color.Transparent),
                center = orb3Center,
                radius = orbRadius * 0.95f
            ),
            radius = orbRadius * 0.95f,
            center = orb3Center
        )
    }
}

/**
 * Diagonal metallic sheen sweep that glides smoothly across cards or buttons.
 */
fun Modifier.shimmerSheen(
    enabled: Boolean = true,
    shimmerColor: Color = Color.White.copy(alpha = 0.42f),
    durationMillis: Int = 2200
): Modifier = composed {
    if (!enabled) return@composed this

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_sheen")
    val progress by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    this.drawWithContent {
        drawContent()
        val width = size.width
        val height = size.height
        val bandWidth = width * 0.42f
        val startX = progress * (width + bandWidth) - bandWidth
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    shimmerColor.copy(alpha = shimmerColor.alpha * 0.5f),
                    shimmerColor,
                    shimmerColor.copy(alpha = shimmerColor.alpha * 0.5f),
                    Color.Transparent
                ),
                start = Offset(startX, 0f),
                end = Offset(startX + bandWidth, height)
            )
        )
    }
}

/**
 * Tactile spring scale-on-press clickable modifier for playful, responsive UI.
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bouncy_scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.foundation.LocalIndication.current,
            enabled = enabled,
            onClick = onClick
        )
}
