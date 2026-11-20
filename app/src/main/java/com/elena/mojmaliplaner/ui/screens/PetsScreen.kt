package com.elena.mojmaliplaner.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.StickerCatalog
import com.elena.mojmaliplaner.data.db.PetLogEntity
import com.elena.mojmaliplaner.data.db.UnlockedStickerEntity
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import com.elena.mojmaliplaner.ui.theme.SoftGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import kotlin.math.sin
import kotlin.random.Random

private data class FloatingHeartParticle(
    val id: Long,
    val emoji: String,
    val startOffsetX: Float,
    val waveAmplitude: Float,
    val driftX: Float,
    val sizeSp: Float
)

private data class PetMoodState(
    val labelSr: String,
    val labelEn: String,
    val moodEmoji: String,
    val progress: Float,
    val badgeColor: Color
)

private fun computePetMood(todayActionsCount: Int): PetMoodState = when {
    todayActionsCount <= 0 -> PetMoodState(
        labelSr = "Pospan / Čeka brigu",
        labelEn = "Sleepy / Needs care",
        moodEmoji = "😴",
        progress = 0.25f,
        badgeColor = Color(0xFFB39DDB)
    )
    todayActionsCount == 1 -> PetMoodState(
        labelSr = "Radoznao",
        labelEn = "Curious",
        moodEmoji = "🙂",
        progress = 0.50f,
        badgeColor = PastelBlue
    )
    todayActionsCount in 2..3 -> PetMoodState(
        labelSr = "Srećan",
        labelEn = "Happy",
        moodEmoji = "😊",
        progress = 0.78f,
        badgeColor = Color(0xFF81C784)
    )
    else -> PetMoodState(
        labelSr = "Presrećan!",
        labelEn = "Overjoyed!",
        moodEmoji = "🥰",
        progress = 1.0f,
        badgeColor = Color(0xFFF48FB1)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    catName: String,
    dogName: String,
    petLogs: List<PetLogEntity>,
    unlockedStickers: List<UnlockedStickerEntity>,
    onLogPetAction: (petType: String, actionType: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedPetProfile by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val isSerbia = LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.pets_topbar),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val displayDogName = if (!isSerbia && dogName == "Bleki") "Blacky" else dogName

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.pets_subtitle, catName, displayDogName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Pet 1: Mačka
            item {
                PetTamagotchiCard(
                    petName = if (isSerbia) "Mačka $catName 🐈‍⬛" else "Cat $catName 🐈‍⬛",
                    petType = "MIA_MACKA",
                    petEmoji = "🐱",
                    petColor = PastelPurple,
                    actions = listOf("HRANA", "VODA", "CETKANJE", "MAZENJE"),
                    actionLabels = if (isSerbia) listOf("Nahrani 🍖", "Daj vodu 💧", "Očešljaj 🪮", "Pomazi 💖") else listOf("Feed 🍖", "Water 💧", "Brush 🪮", "Cuddle 💖"),
                    petLogs = petLogs.filter { it.petType == "MIA_MACKA" },
                    onActionClick = { action ->
                        onLogPetAction("MIA_MACKA", action)
                        Toast.makeText(context, "$catName ${if (isSerbia) "je srećna!" else "is happy!"} ✨", Toast.LENGTH_SHORT).show()
                    },
                    onProfileClick = { selectedPetProfile = "MIA_MACKA" }
                )
            }

            // Pet 2: Pas
            item {
                PetTamagotchiCard(
                    petName = if (isSerbia) "Pas $displayDogName 🐕" else "Dog $displayDogName 🐕",
                    petType = "BLEKI_PAS",
                    petEmoji = "🐶",
                    petColor = PastelBlue,
                    actions = listOf("HRANA", "VODA", "SETNJA", "MAZENJE"),
                    actionLabels = if (isSerbia) listOf("Nahrani 🍖", "Daj vodu 💧", "U šetnju 🦮", "Igraj se 🎾") else listOf("Feed 🍖", "Water 💧", "Walk 🦮", "Play 🎾"),
                    petLogs = petLogs.filter { it.petType == "BLEKI_PAS" },
                    onActionClick = { action ->
                        onLogPetAction("BLEKI_PAS", action)
                        Toast.makeText(context, "$displayDogName ${if (isSerbia) "vrti repom!" else "wags its tail!"} ✨", Toast.LENGTH_SHORT).show()
                    },
                    onProfileClick = { selectedPetProfile = "BLEKI_PAS" }
                )
            }

            // Unlocked Pets
            val unlockedIds = unlockedStickers.map { it.stickerId }
            val specialPets = StickerCatalog.STICKERS.filter { it.unlocksPet && unlockedIds.contains(it.id) }

            items(specialPets, key = { it.id }) { animal ->
                val displayName = if (isSerbia) animal.nameSr else animal.nameEn
                val typeId = "PET_${animal.id}"

                PetTamagotchiCard(
                    petName = "$displayName ${animal.emoji}",
                    petType = typeId,
                    petEmoji = animal.emoji,
                    petColor = SoftGold,
                    actions = listOf("HRANA", "VODA", "MAZENJE", "IGRA"),
                    actionLabels = if (isSerbia) listOf("Nahrani 🍖", "Daj vodu 💧", "Pomazi 💖", "Igra 🎾") else listOf("Feed 🍖", "Water 💧", "Cuddle 💖", "Play 🎾"),
                    petLogs = petLogs.filter { it.petType == typeId },
                    onActionClick = { action ->
                        onLogPetAction(typeId, action)
                        Toast.makeText(context, "$displayName ${if (isSerbia) "je srećan/na!" else "is happy!"} ✨", Toast.LENGTH_SHORT).show()
                    },
                    onProfileClick = { selectedPetProfile = typeId }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (selectedPetProfile != null) {
        val typeId = selectedPetProfile!!
        val displayDogName = if (!isSerbia && dogName == "Bleki") "Blacky" else dogName
        val petName = when (typeId) {
            "MIA_MACKA" -> if (isSerbia) "Mačka $catName 🐈‍⬛" else "Cat $catName 🐈‍⬛"
            "BLEKI_PAS" -> if (isSerbia) "Pas $displayDogName 🐕" else "Dog $displayDogName 🐕"
            else -> {
                val animal = StickerCatalog.STICKERS.find { "PET_${it.id}" == typeId }
                val dName = animal?.let { if (isSerbia) it.nameSr else it.nameEn } ?: "Pet"
                val emo = animal?.emoji ?: "🐾"
                "$dName $emo"
            }
        }
        val logs = petLogs.filter { it.petType == selectedPetProfile }

        Dialog(onDismissRequest = { selectedPetProfile = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSerbia) "Profil: $petName" else "Profile: $petName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { selectedPetProfile = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Zatvori")
                        }
                    }

                    Text(
                        text = if (isSerbia) "Istorija brige i aktivnosti 📋" else "Care & Activity History 📋",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (logs.isEmpty()) {
                        Text(
                            text = if (isSerbia) "Još nema zabeleženih aktivnosti za danas." else "No activities recorded for today yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            logs.take(5).forEach { log ->
                                val dateStr = SimpleDateFormat("HH:mm - dd.MM.", if (isSerbia) Locale("sr") else Locale.ENGLISH).format(Date(log.timestampMillis))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${if (isSerbia) "Aktivnost" else "Activity"}: ${log.actionType}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetTamagotchiCard(
    petName: String,
    petType: String,
    petEmoji: String,
    petColor: Color,
    actions: List<String>,
    actionLabels: List<String>,
    petLogs: List<PetLogEntity>,
    onActionClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val isSerbia = LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)
    val coroutineScope = rememberCoroutineScope()

    // Count today's care actions for live mood calculation
    val today = remember { LocalDate.now() }
    val todayCount = remember(petLogs) {
        val zone = ZoneId.systemDefault()
        petLogs.count {
            Instant.ofEpochMilli(it.timestampMillis).atZone(zone).toLocalDate() == today
        }
    }
    val mood = remember(todayCount) { computePetMood(todayCount) }
    val animatedHappiness by animateFloatAsState(
        targetValue = mood.progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pet_happiness_$petType"
    )
    val animatedMoodColor by animateColorAsState(
        targetValue = mood.badgeColor,
        animationSpec = tween(350),
        label = "pet_mood_color_$petType"
    )

    // Idle gentle breathing bob
    val idleTransition = rememberInfiniteTransition(label = "pet_idle_$petType")
    val idleBob by idleTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_bob"
    )

    // Joyful jump + wiggle when user cares for the pet
    val jumpOffsetY = remember { Animatable(0f) }
    val wiggleAngle = remember { Animatable(0f) }

    // Floating hearts particle list
    val floatingHearts = remember { mutableStateListOf<FloatingHeartParticle>() }
    var nextParticleId by remember { mutableLongStateOf(0L) }

    fun spawnHeartsAndBounce(actionType: String) {
        val actionEmoji = when (actionType) {
            "HRANA" -> "🍖"
            "VODA" -> "💧"
            "CETKANJE" -> "✨"
            "SETNJA" -> "🐾"
            "IGRA" -> "🎾"
            else -> "💖"
        }
        val pool = listOf("💖", "💕", "💗", actionEmoji, "✨")

        // Spawn 6 staggered floating hearts
        coroutineScope.launch {
            repeat(6) { idx ->
                val id = ++nextParticleId
                val particle = FloatingHeartParticle(
                    id = id,
                    emoji = pool[idx % pool.size],
                    startOffsetX = Random.nextFloat() * 48f - 24f,
                    waveAmplitude = 10f + Random.nextFloat() * 16f,
                    driftX = Random.nextFloat() * 34f - 17f,
                    sizeSp = 17f + Random.nextFloat() * 8f
                )
                floatingHearts.add(particle)
                delay(65L)
            }
        }

        // Joyful pet jump + wiggle
        coroutineScope.launch {
            jumpOffsetY.animateTo(-18f, animationSpec = tween(130, easing = FastOutSlowInEasing))
            jumpOffsetY.animateTo(
                0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        coroutineScope.launch {
            listOf(-14f, 14f, -8f, 8f, 0f).forEach { deg ->
                wiggleAngle.animateTo(deg, animationSpec = tween(65))
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.8.dp, petColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            petColor.copy(alpha = 0.14f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(
                                modifier = Modifier
                                    .size(56.dp)
                                    .graphicsLayer {
                                        translationY = idleBob + jumpOffsetY.value
                                        rotationZ = wiggleAngle.value
                                    },
                                shape = CircleShape,
                                color = petColor.copy(alpha = 0.28f),
                                border = BorderStroke(1.5.dp, petColor)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = petEmoji, fontSize = 30.sp)
                                }
                            }

                            // Mood mini badge tucked on the corner of avatar
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .size(24.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, animatedMoodColor),
                                shadowElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AnimatedContent(
                                        targetState = mood.moodEmoji,
                                        transitionSpec = {
                                            (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn())
                                                .togetherWith(scaleOut() + fadeOut())
                                        },
                                        label = "mood_emoji_anim"
                                    ) { emo ->
                                        Text(text = emo, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = petName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = animatedMoodColor.copy(alpha = 0.24f),
                                    border = BorderStroke(1.dp, animatedMoodColor.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "${mood.moodEmoji} ${if (isSerbia) mood.labelSr else mood.labelEn}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onProfileClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isSerbia) "Profil" else "Profile", fontWeight = FontWeight.Bold)
                    }
                }

                // Animated Happiness Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSerbia) "Raspoloženje 💖 (${(animatedHappiness * 100).toInt()}%)" else "Happiness 💖 (${(animatedHappiness * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isSerbia) "Danas: $todayCount • Ukupno: ${petLogs.size}" else "Today: $todayCount • Total: ${petLogs.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { animatedHappiness },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = animatedMoodColor,
                        trackColor = animatedMoodColor.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                }

                // Care Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in actions.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val action1 = actions[i]
                            val label1 = actionLabels[i]

                            Button(
                                onClick = {
                                    spawnHeartsAndBounce(action1)
                                    onActionClick(action1)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = petColor,
                                    contentColor = BlackCharcoal
                                )
                            ) {
                                Text(label1, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            if (i + 1 < actions.size) {
                                val action2 = actions[i + 1]
                                val label2 = actionLabels[i + 1]

                                Button(
                                    onClick = {
                                        spawnHeartsAndBounce(action2)
                                        onActionClick(action2)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = petColor,
                                        contentColor = BlackCharcoal
                                    )
                                ) {
                                    Text(label2, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Floating Hearts Particle Layer rising gracefully over the card
            floatingHearts.forEach { particle ->
                FloatingHeartItem(
                    particle = particle,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 34.dp, top = 44.dp),
                    onFinished = { floatingHearts.remove(particle) }
                )
            }
        }
    }
}

@Composable
private fun FloatingHeartItem(
    particle: FloatingHeartParticle,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val progress = remember(particle.id) { Animatable(0f) }

    LaunchedEffect(particle.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1250, easing = FastOutSlowInEasing)
        )
        onFinished()
    }

    val p = progress.value
    val offsetY = -115f * p
    val offsetX = particle.startOffsetX +
        particle.driftX * p +
        sin(p * 7f) * particle.waveAmplitude
    val alpha = when {
        p < 0.15f -> p / 0.15f
        p > 0.65f -> (1f - (p - 0.65f) / 0.35f).coerceIn(0f, 1f)
        else -> 1f
    }
    val scale = 0.65f + 0.55f * sin(p * Math.PI.toFloat()).coerceAtLeast(0f)

    Text(
        text = particle.emoji,
        fontSize = particle.sizeSp.sp,
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX
                translationY = offsetY
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
    )
}
