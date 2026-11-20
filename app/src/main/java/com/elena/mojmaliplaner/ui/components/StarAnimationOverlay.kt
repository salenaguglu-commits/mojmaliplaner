package com.elena.mojmaliplaner.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.SoftGold
import com.elena.mojmaliplaner.ui.viewmodel.MainViewModel
import com.elena.mojmaliplaner.ui.viewmodel.StarEvent
import com.elena.mojmaliplaner.ui.viewmodel.StarType
import kotlinx.coroutines.flow.collectLatest

val BronzeColor = Color(0xFFCD7F32)
val SilverColor = Color(0xFFB0BEC5)
val GoldColor = SoftGold

@Composable
fun StarAnimationOverlay(viewModel: MainViewModel) {
    var activeEvent by remember { mutableStateOf<StarEvent?>(null) }
    var eventCounter by remember { mutableIntStateOf(0) }
    val isSerbia = LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)

    LaunchedEffect(Unit) {
        viewModel.starEvents.collectLatest { event ->
            eventCounter++
            activeEvent = event
        }
    }

    activeEvent?.let { event ->
        Dialog(
            onDismissRequest = { activeEvent = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val entranceScale = remember(eventCounter) { Animatable(0.55f) }
            LaunchedEffect(eventCounter) {
                entranceScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            val infiniteTransition = rememberInfiniteTransition(label = "star_celebration")
            val starScale by infiniteTransition.animateFloat(
                initialValue = 0.92f,
                targetValue = 1.18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(550, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val starTilt by infiniteTransition.animateFloat(
                initialValue = -7f,
                targetValue = 7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "tilt"
            )

            val (emoji, borderColor, starName) = when (event.type) {
                StarType.BRONZE -> Triple("🥉", BronzeColor, if (isSerbia) "Bronzana Zvezdica" else "Bronze Star")
                StarType.SILVER -> Triple("🥈", SilverColor, if (isSerbia) "Srebrna Zvezdica" else "Silver Star")
                StarType.GOLD -> Triple("⭐", GoldColor, if (isSerbia) "Zlatna Zvezdica" else "Gold Star")
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Full-screen celebratory confetti burst!
                ConfettiBurstOverlay(
                    triggerKey = eventCounter,
                    particleCount = if (event.type == StarType.GOLD) 95 else 68,
                    originYFraction = 0.42f
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .scale(entranceScale.value),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.5.dp, borderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        borderColor.copy(alpha = 0.16f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(152.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SunburstGlow(
                                    color = borderColor,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    modifier = Modifier.size(104.dp),
                                    shape = CircleShape,
                                    color = borderColor.copy(alpha = 0.22f),
                                    border = BorderStroke(2.dp, borderColor.copy(alpha = 0.65f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = emoji,
                                            fontSize = 62.sp,
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = starScale
                                                scaleY = starScale
                                                rotationZ = starTilt
                                            }
                                        )
                                    }
                                }
                            }

                            Text(
                                text = starName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = borderColor,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (isSerbia) event.messageSr else event.messageEn,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { activeEvent = null },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = borderColor,
                                    contentColor = BlackCharcoal
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .shimmerSheen(
                                        enabled = event.type == StarType.GOLD,
                                        shimmerColor = Color.White.copy(alpha = 0.45f)
                                    )
                            ) {
                                Text(
                                    text = if (isSerbia) "Super! ✨" else "Awesome! ✨",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
