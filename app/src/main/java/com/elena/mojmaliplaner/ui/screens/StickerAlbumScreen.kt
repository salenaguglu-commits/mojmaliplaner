package com.elena.mojmaliplaner.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.StickerAnimal
import com.elena.mojmaliplaner.data.StickerCatalog
import com.elena.mojmaliplaner.data.db.UnlockedStickerEntity
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.SoftGold
import java.io.File
import java.io.FileOutputStream

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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isSerbia) "Album Čudnih Životinja" else "Weird Animals Album",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalUnlocked / 20",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
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
                        enabled = totalUnlocked < 20
                    ) {
                        Text(
                            text = if (isSerbia) "Otvori paket (3 ⭐)" else "Open Pack (3 ⭐)",
                            fontWeight = FontWeight.Bold
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(85.dp)
                            .clickable(enabled = isUnlocked) {
                                showDetailDialog = animal
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                Text(text = animal.emoji, fontSize = 42.sp)
                                Text(
                                    text = "#${animal.id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Text(text = "?", fontSize = 32.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    showDetailDialog?.let { animal ->
        Dialog(onDismissRequest = { showDetailDialog = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = SoftGold,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isSerbia) animal.rarity.labelSr else animal.rarity.labelEn,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BlackCharcoal
                            )
                        }
                        IconButton(onClick = { showDetailDialog = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Zatvori")
                        }
                    }

                    Text(text = animal.emoji, fontSize = 100.sp)
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
                            // Simulated share text
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSerbia) "Podeli (Share)" else "Share", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Scratch Dialog
    showScratchDialog?.let { stickerId ->
        val animal = StickerCatalog.STICKERS.find { it.id == stickerId }
        var isScratched by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showScratchDialog = null }) {
            Card(
                modifier = Modifier.size(300.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (animal != null) {
                        // Background / Revealed
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = animal.emoji, fontSize = 100.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (isSerbia) animal.nameSr else animal.nameEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (animal.unlocksPet) {
                                Text(
                                    text = if (isSerbia) "Novi ljubimac otključan!" else "New pet unlocked!",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        // Scratch overlay
                        if (!isScratched) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ScratchCardOverlay(
                                    modifier = Modifier.fillMaxSize(),
                                    onScratchComplete = { isScratched = true }
                                )
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 12.dp)
                                        .clickable { isScratched = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.Black.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = if (isSerbia) "Ogrebi prstom ili klikni ovde 🪙" else "Scratch or click here 🪙",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        } else {
                            LaunchedEffect(Unit) {
                                Toast.makeText(context, if (isSerbia) "Otkriveno!" else "Revealed!", Toast.LENGTH_SHORT).show()
                            }
                            IconButton(
                                onClick = { showScratchDialog = null },
                                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
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

@Composable
fun ScratchCardOverlay(
    modifier: Modifier = Modifier,
    onScratchComplete: () -> Unit
) {
    val path = remember { Path() }
    var touchCount by remember { mutableIntStateOf(0) }

    Canvas(
        modifier = modifier
            .graphicsLayer { alpha = 0.99f }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        path.moveTo(offset.x, offset.y)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        path.lineTo(change.position.x, change.position.y)
                        touchCount++
                        if (touchCount > 35) {
                            onScratchComplete()
                        }
                    }
                )
            }
    ) {
        // Foil Background (Shiny Gold/Pastel Foil)
        drawRect(color = Color(0xFFD4E157), size = size)

        // Scratched Path
        drawPath(
            path = path,
            color = Color.Transparent,
            style = Stroke(width = 120f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            blendMode = BlendMode.Clear
        )
    }
}
