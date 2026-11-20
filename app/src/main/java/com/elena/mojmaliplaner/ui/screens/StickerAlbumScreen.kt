package com.elena.mojmaliplaner.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elena.mojmaliplaner.data.StickerAnimal
import com.elena.mojmaliplaner.data.StickerCatalog
import com.elena.mojmaliplaner.data.StickerRarity
import com.elena.mojmaliplaner.data.db.UnlockedStickerEntity
import com.elena.mojmaliplaner.ui.components.ConfettiBurstOverlay
import com.elena.mojmaliplaner.ui.components.SunburstGlow
import com.elena.mojmaliplaner.ui.components.bouncyClickable
import com.elena.mojmaliplaner.ui.components.drawSparkleStar
import com.elena.mojmaliplaner.ui.components.shimmerSheen
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import com.elena.mojmaliplaner.ui.theme.SoftGold

private fun rarityColor(rarity: StickerRarity): Color = when (rarity) {
    StickerRarity.COMMON -> PastelBlue
    StickerRarity.RARE -> PastelPurple
    StickerRarity.LEGENDARY -> SoftGold
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerAlbumScreen(
    availableStars: Int,
    unlockedStickers: List<UnlockedStickerEntity>,
    onOpenPack: (onOpened: (Int) -> Unit, onNotEnoughStars: () -> Unit) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isSerbia = androidx.compose.ui.platform.LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)
    val unlockedIds = unlockedStickers.map { it.stickerId }
    val totalUnlocked = unlockedStickers.size
    val albumProgress by animateFloatAsState(
        targetValue = (totalUnlocked / 20f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "album_progress"
    )

    var showScratchDialog by remember { mutableStateOf<Int?>(null) }
    var showDetailDialog by remember { mutableStateOf<StickerAnimal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSerbia) "Sličice 🐾" else "Stickers 🐾",
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
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, SoftGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⭐", fontSize = 16.sp)
                            Text(
                                text = "$availableStars",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(end = 12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info & Open Pack Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, SoftGold.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    SoftGold.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isSerbia) "Album Čudnih Životinja" else "Weird Animals Album",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalUnlocked / 20",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    LinearProgressIndicator(
                        progress = { albumProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = SoftGold,
                        trackColor = SoftGold.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )

                    val canOpenPack = totalUnlocked < 20 && availableStars >= 3
                    Button(
                        onClick = {
                            if (totalUnlocked >= 20) {
                                Toast.makeText(context, if (isSerbia) "Sve su otključane!" else "All unlocked!", Toast.LENGTH_SHORT).show()
                            } else {
                                onOpenPack(
                                    { newStickerId -> showScratchDialog = newStickerId },
                                    { Toast.makeText(context, if (isSerbia) "Nemaš dovoljno zvezdica (potrebno 3)!" else "Not enough stars (need 3)!", Toast.LENGTH_SHORT).show() }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = BlackCharcoal),
                        shape = RoundedCornerShape(16.dp),
                        enabled = totalUnlocked < 20,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .shimmerSheen(
                                enabled = canOpenPack,
                                shimmerColor = Color.White.copy(alpha = 0.5f),
                                durationMillis = 1900
                            )
                    ) {
                        Text(
                            text = if (isSerbia) "Otvori paket (3 ⭐)" else "Open Pack (3 ⭐)",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Grid of 20 stickers
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(StickerCatalog.STICKERS, key = { it.id }) { animal ->
                    val isUnlocked = unlockedIds.contains(animal.id)
                    val accent = rarityColor(animal.rarity)
                    val isLegendary = animal.rarity == StickerRarity.LEGENDARY

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .bouncyClickable(enabled = isUnlocked, pressedScale = 0.92f) {
                                showDetailDialog = animal
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            width = if (isUnlocked && isLegendary) 2.dp else 1.2.dp,
                            color = if (isUnlocked) accent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 3.dp else 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (isUnlocked) {
                                        Modifier
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        accent.copy(alpha = 0.28f),
                                                        accent.copy(alpha = 0.08f)
                                                    )
                                                )
                                            )
                                            .shimmerSheen(
                                                enabled = isLegendary,
                                                shimmerColor = Color.White.copy(alpha = 0.35f),
                                                durationMillis = 2800
                                            )
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                Text(text = animal.emoji, fontSize = 40.sp)
                                Text(
                                    text = "#${animal.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                                if (isLegendary) {
                                    Text(
                                        text = "✨",
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "?",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    showDetailDialog?.let { animal ->
        val accent = rarityColor(animal.rarity)
        Dialog(onDismissRequest = { showDetailDialog = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, accent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = accent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isSerbia) animal.rarity.labelSr else animal.rarity.labelEn,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BlackCharcoal
                            )
                        }
                        IconButton(onClick = { showDetailDialog = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Zatvori")
                        }
                    }

                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SunburstGlow(color = accent, modifier = Modifier.fillMaxSize())
                        Text(text = animal.emoji, fontSize = 90.sp)
                    }

                    Text(
                        text = if (isSerbia) animal.nameSr else animal.nameEn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (animal.unlocksPet) {
                        Text(
                            text = if (isSerbia) "🔓 Otključava ljubimca!" else "🔓 Unlocks a pet!",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        val trivia = if (isSerbia) animal.triviaSr else animal.triviaEn
                        trivia.forEach { fact ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("💡", modifier = Modifier.padding(end = 8.dp))
                                Text(text = fact, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, if (isSerbia) "Deljenje..." else "Sharing...", Toast.LENGTH_SHORT).show()
                            val shareText = if (isSerbia) {
                                "Pogledaj ovu životinju iz Mog Malog Planera!\n${animal.nameSr} ${animal.emoji}\n💡 ${animal.triviaSr.first()}"
                            } else {
                                "Look at this animal from My Little Planner!\n${animal.nameEn} ${animal.emoji}\n💡 ${animal.triviaEn.first()}"
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, if (isSerbia) "Podeli sličicu" else "Share sticker"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSerbia) "Podeli (Share)" else "Share", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Scratch Dialog with Shimmer Foil & Reveal Confetti!
    showScratchDialog?.let { stickerId ->
        val animal = StickerCatalog.STICKERS.find { it.id == stickerId }
        var isScratched by remember(stickerId) { mutableStateOf(false) }
        var scratchProgress by remember(stickerId) { mutableStateOf(0f) }

        val revealPop = remember(stickerId) { Animatable(0.85f) }
        LaunchedEffect(isScratched) {
            if (isScratched) {
                revealPop.animateTo(
                    targetValue = 1.06f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                revealPop.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy))
            }
        }

        Dialog(
            onDismissRequest = { showScratchDialog = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isScratched) {
                    ConfettiBurstOverlay(
                        triggerKey = stickerId,
                        particleCount = 84,
                        originYFraction = 0.45f
                    )
                }

                Card(
                    modifier = Modifier
                        .size(316.dp)
                        .scale(if (isScratched) revealPop.value else 1f),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        width = 2.5.dp,
                        color = animal?.let { rarityColor(it.rarity) } ?: SoftGold
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (animal != null) {
                            val accent = rarityColor(animal.rarity)

                            // Background / Revealed Sticker
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                accent.copy(alpha = 0.25f),
                                                MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isScratched) {
                                    SunburstGlow(
                                        color = accent,
                                        modifier = Modifier.size(230.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Surface(
                                        color = accent,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (isSerbia) animal.rarity.labelSr else animal.rarity.labelEn,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BlackCharcoal
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = animal.emoji, fontSize = 96.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = if (isSerbia) animal.nameSr else animal.nameEn,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    if (animal.unlocksPet) {
                                        Text(
                                            text = if (isSerbia) "🔓 Novi ljubimac otključan!" else "🔓 New pet unlocked!",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }

                            // Metallic Shimmer Scratch Overlay
                            if (!isScratched) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ScratchCardOverlay(
                                        modifier = Modifier.fillMaxSize(),
                                        onScratchProgress = { scratchProgress = it },
                                        onScratchComplete = { isScratched = true }
                                    )

                                    if (scratchProgress < 0.08f) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = "✨ 🎁 ✨", fontSize = 36.sp)
                                            Text(
                                                text = if (isSerbia) "Ogrebi zlatnu foliju!" else "Scratch the gold foil!",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = BlackCharcoal
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 14.dp)
                                            .clickable { isScratched = true },
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.Black.copy(alpha = 0.72f)
                                    ) {
                                        Text(
                                            text = if (isSerbia) "Ogrebi prstom ili klikni ovde 🪙" else "Scratch or tap here 🪙",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            } else {
                                LaunchedEffect(Unit) {
                                    Toast.makeText(context, if (isSerbia) "Otkriveno! ✨" else "Revealed! ✨", Toast.LENGTH_SHORT).show()
                                }
                                IconButton(
                                    onClick = { showScratchDialog = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Zatvori")
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
fun ScratchCardOverlay(
    modifier: Modifier = Modifier,
    onScratchProgress: (Float) -> Unit = {},
    onScratchComplete: () -> Unit
) {
    val path = remember { Path() }
    var touchCount by remember { mutableIntStateOf(0) }
    val scratchSparkles = remember { mutableStateListOf<Offset>() }

    // Continuous metallic foil shimmer sweep + twinkling stars
    val infiniteTransition = rememberInfiniteTransition(label = "scratch_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1750, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "foil_twinkle"
    )

    Canvas(
        modifier = modifier
            .graphicsLayer { alpha = 0.99f }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        path.moveTo(offset.x, offset.y)
                        scratchSparkles.add(offset)
                        if (scratchSparkles.size > 16) scratchSparkles.removeAt(0)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val pos = change.position
                        path.lineTo(pos.x, pos.y)
                        touchCount++
                        if (touchCount % 3 == 0) {
                            scratchSparkles.add(pos)
                            if (scratchSparkles.size > 16) scratchSparkles.removeAt(0)
                        }
                        onScratchProgress((touchCount / 36f).coerceIn(0f, 1f))
                        if (touchCount > 35) {
                            onScratchComplete()
                        }
                    }
                )
            }
    ) {
        // Trigger recomposition cleanly as touchCount increases
        val currentTouches = touchCount

        // 1. Rich Multi-Stop Metallic Gold Foil Base
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFC8A143),
                    Color(0xFFEBD07A),
                    Color(0xFFD2AD4F),
                    Color(0xFFF3DE99),
                    Color(0xFFC29B3C)
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            ),
            size = size
        )

        // 2. Animated Diagonal Specular Shimmer Band
        val bandWidth = size.width * 0.48f
        val startX = shimmerOffset * (size.width + bandWidth) - bandWidth
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFFFFF8DC).copy(alpha = 0.38f),
                    Color.White.copy(alpha = 0.78f),
                    Color(0xFFFFF8DC).copy(alpha = 0.38f),
                    Color.Transparent
                ),
                start = Offset(startX, 0f),
                end = Offset(startX + bandWidth, size.height)
            ),
            size = size
        )

        // 3. Twinkling Foil Sparkles embedded on the gold layer
        val starCoords = listOf(
            Offset(size.width * 0.16f, size.height * 0.20f),
            Offset(size.width * 0.82f, size.height * 0.18f),
            Offset(size.width * 0.22f, size.height * 0.74f),
            Offset(size.width * 0.80f, size.height * 0.72f),
            Offset(size.width * 0.50f, size.height * 0.14f)
        )
        starCoords.forEachIndexed { idx, pt ->
            val phaseAlpha = if (idx % 2 == 0) twinkle else (1.25f - twinkle).coerceIn(0.25f, 1f)
            drawSparkleStar(
                center = pt,
                radius = 12f + (idx % 3) * 4f,
                color = Color.White.copy(alpha = phaseAlpha * 0.85f)
            )
        }

        // 4. Scratched Path clears away the gold shimmer foil cleanly
        if (currentTouches >= 0) {
            drawPath(
                path = path,
                color = Color.Transparent,
                style = Stroke(width = 120f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                blendMode = BlendMode.Clear
            )
        }

        // 5. Glinting scratch dust sparkles along recent finger trail
        scratchSparkles.forEachIndexed { i, pt ->
            val alpha = ((i + 1).toFloat() / scratchSparkles.size.coerceAtLeast(1)) * 0.85f
            drawSparkleStar(
                center = Offset(pt.x + (i % 3 - 1) * 14f, pt.y + ((i + 1) % 3 - 1) * 14f),
                radius = 7f,
                color = Color(0xFFFFF3C4).copy(alpha = alpha)
            )
        }
    }
}
