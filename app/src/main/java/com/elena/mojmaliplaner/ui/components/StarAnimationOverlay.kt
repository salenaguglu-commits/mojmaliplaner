package com.elena.mojmaliplaner.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    val isSerbia = LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)

    LaunchedEffect(Unit) {
        viewModel.starEvents.collectLatest { event ->
            activeEvent = event
        }
    }

    activeEvent?.let { event ->
        Dialog(onDismissRequest = { activeEvent = null }) {
            val infiniteTransition = rememberInfiniteTransition(label = "star_scale")
            val starScale by infiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            val (emoji, borderColor, starName) = when (event.type) {
                StarType.BRONZE -> Triple("🥉", BronzeColor, if (isSerbia) "Bronzana Zvezdica" else "Bronze Star")
                StarType.SILVER -> Triple("🥈", SilverColor, if (isSerbia) "Srebrna Zvezdica" else "Silver Star")
                StarType.GOLD -> Triple("⭐", GoldColor, if (isSerbia) "Zlatna Zvezdica" else "Gold Star")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = emoji,
                        fontSize = 90.sp,
                        modifier = Modifier.scale(starScale)
                    )

                    Text(
                        text = starName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = borderColor,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isSerbia) event.messageSr else event.messageEn,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { activeEvent = null },
                        colors = ButtonDefaults.buttonColors(containerColor = borderColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSerbia) "Super! ✨" else "Awesome! ✨", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
