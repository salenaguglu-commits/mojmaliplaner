package com.elena.mojmaliplaner.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.db.CustomTrackEntity
import com.elena.mojmaliplaner.data.db.MusicSheetEntity
import com.elena.mojmaliplaner.ui.components.bouncyClickable
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import com.elena.mojmaliplaner.ui.theme.SoftGold
import com.elena.mojmaliplaner.ui.viewmodel.AudioTrack
import com.elena.mojmaliplaner.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private fun formatMillis(ms: Int): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val mins = totalSec / 60
    val secs = totalSec % 60
    return "%d:%02d".format(mins, secs)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    musicViewModel: MusicViewModel,
    musicSheets: List<MusicSheetEntity>,
    customTracks: List<CustomTrackEntity>,
    onAddCustomTrackUri: (Uri) -> Unit,
    onDeleteCustomTrack: (CustomTrackEntity) -> Unit,
    onAddMusicSheet: (title: String, category: String, fileUri: String) -> Unit,
    onDeleteMusicSheet: (MusicSheetEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val isSerbia = LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)

    val isPlaying by musicViewModel.isPlaying.collectAsState()
    val currentTrack by musicViewModel.currentTrack.collectAsState()
    val volume by musicViewModel.volume.collectAsState()
    val sleepTimerMinutes by musicViewModel.sleepTimerMinutes.collectAsState()
    val remainingSeconds by musicViewModel.remainingSeconds.collectAsState()
    val currentPositionMs by musicViewModel.currentPositionMs.collectAsState()
    val trackDurationMs by musicViewModel.trackDurationMs.collectAsState()

    val rawPlaybackProgress = (currentPositionMs.toFloat() / trackDurationMs.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val smoothProgress by animateFloatAsState(
        targetValue = if (currentTrack != null) rawPlaybackProgress else 0f,
        animationSpec = tween(220, easing = LinearEasing),
        label = "smooth_playback_progress"
    )

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var newSheetTitle by remember { mutableStateOf("") }
    val newSheetCategory = if (isSerbia) "Solfeđo & Note" else "Music Notes"
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, if (isSerbia) "Slika nota izabrana! ✨" else "Sheet music photo selected! ✨", Toast.LENGTH_SHORT).show()
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onAddCustomTrackUri(uri)
            Toast.makeText(context, if (isSerbia) "Pesma se dodaje u vašu listu... 🎵✨" else "Adding song to your list... 🎵✨", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.music_topbar),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Cozy Lofi Player 🎧", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text(if (isSerbia) "Muzička škola & Note 🎼" else "Music School & Notes 🎼", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTabIndex) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AnimatedMusicPlayerCard(
                                currentTrack = currentTrack,
                                isPlaying = isPlaying,
                                playbackProgress = smoothProgress,
                                currentPositionMs = currentPositionMs,
                                trackDurationMs = trackDurationMs,
                                volume = volume,
                                sleepTimerMinutes = sleepTimerMinutes,
                                remainingSeconds = remainingSeconds,
                                isSerbia = isSerbia,
                                onTogglePlayPause = { musicViewModel.togglePlayPause() },
                                onVolumeChange = { musicViewModel.setVolume(it) },
                                onSetSleepTimer = { musicViewModel.setSleepTimer(it) }
                            )
                        }

                        // Lofi Tracks Section
                        item {
                            Text(
                                text = if (isSerbia) "Lofi / Chill Melodije 🎶" else "Lofi / Chill Tunes 🎶",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        items(musicViewModel.factoryTracks.filter { it.category == "LOFI" }) { track ->
                            TrackCard(
                                track = track,
                                isCurrent = currentTrack?.id == track.id,
                                isPlaying = isPlaying && currentTrack?.id == track.id,
                                onSelect = { musicViewModel.playTrack(track) },
                                onDelete = null
                            )
                        }

                        // Ambience Section
                        item {
                            Text(
                                text = if (isSerbia) "Zvučni Ambijenti i Efekti 🌧️🐾" else "Sound Ambience & FX 🌧️🐾",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(musicViewModel.factoryTracks.filter { it.category == "AMBIENCE" }) { track ->
                            TrackCard(
                                track = track,
                                isCurrent = currentTrack?.id == track.id,
                                isPlaying = isPlaying && currentTrack?.id == track.id,
                                onSelect = { musicViewModel.playTrack(track) },
                                onDelete = null
                            )
                        }

                        // Custom MP3 Tracks Section
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isSerbia) "Moje Pesme (MP3) 🎵" else "My Songs (MP3) 🎵",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Button(
                                    onClick = { audioPickerLauncher.launch("audio/*") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SoftGold,
                                        contentColor = BlackCharcoal
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj MP3")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isSerbia) "+ Dodaj pesmu" else "+ Add song", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        if (customTracks.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Text(
                                        text = if (isSerbia) "Još nema dodatih MP3 pesama. Klikni '+ Dodaj pesmu' da ubaciš tvoju omiljeno muziku! ✨" else "No MP3 songs added yet. Tap '+ Add song' to import your favorite music! ✨",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            items(customTracks, key = { it.id }) { entity ->
                                val track = AudioTrack(
                                    id = "custom_${entity.id}",
                                    name = entity.title,
                                    filePath = entity.filePath,
                                    category = "MOJA_MUZIKA",
                                    emoji = "🎧",
                                    customTrackEntity = entity
                                )

                                TrackCard(
                                    track = track,
                                    isCurrent = currentTrack?.id == track.id,
                                    isPlaying = isPlaying && currentTrack?.id == track.id,
                                    onSelect = { musicViewModel.playTrack(track) },
                                    onDelete = { onDeleteCustomTrack(entity) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showAddNoteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj note")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSerbia) "Dodaj nove note / sliku 🎼" else "Add new sheet music / photo 🎼",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (musicSheets.isEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = if (isSerbia) "Još nema sačuvanih nota u galeriji. Klikni iznad da dodaš sliku svojih nota za solfeđo ili klavir! 🎹✨" else "No sheet music saved in gallery yet. Tap above to add photos of your music notes! 🎹✨",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(musicSheets, key = { it.id }) { sheet ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            if (sheet.fileUri.isNotBlank()) {
                                                AsyncImage(
                                                    model = sheet.fileUri,
                                                    contentDescription = sheet.title,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(130.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(100.dp),
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("🎼", fontSize = 36.sp)
                                                    }
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = sheet.title,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = sheet.instrumentOrCategory,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                IconButton(onClick = { onDeleteMusicSheet(sheet) }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Obriši",
                                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(18.dp)
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
            }
        }
    }

    if (showAddNoteDialog) {
        Dialog(onDismissRequest = { showAddNoteDialog = false }) {
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
                            text = if (isSerbia) "Dodaj nove note 🎼" else "Add new sheet music 🎼",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { showAddNoteDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Zatvori")
                        }
                    }

                    OutlinedTextField(
                        value = newSheetTitle,
                        onValueChange = { newSheetTitle = it },
                        placeholder = { Text(if (isSerbia) "Naziv pesme / vežbe..." else "Title of song / exercise...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (selectedImageUri != null) (if (isSerbia) "Slika nota izabrana ✓" else "Sheet music photo selected ✓") else (if (isSerbia) "🖼️ Izaberi sliku nota iz galerije" else "🖼️ Choose sheet music photo from gallery"),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = {
                            if (newSheetTitle.isNotBlank()) {
                                onAddMusicSheet(
                                    newSheetTitle,
                                    newSheetCategory,
                                    selectedImageUri?.toString() ?: ""
                                )
                                Toast.makeText(context, if (isSerbia) "Note sačuvane! 🎼✨" else "Sheet music saved! 🎼✨", Toast.LENGTH_SHORT).show()
                                newSheetTitle = ""
                                selectedImageUri = null
                                showAddNoteDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isSerbia) "Sačuvaj u galeriju ✨" else "Save to gallery ✨", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedMusicPlayerCard(
    currentTrack: AudioTrack?,
    isPlaying: Boolean,
    playbackProgress: Float,
    currentPositionMs: Int,
    trackDurationMs: Int,
    volume: Float,
    sleepTimerMinutes: Int?,
    remainingSeconds: Int,
    isSerbia: Boolean,
    onTogglePlayPause: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onSetSleepTimer: (Int?) -> Unit
) {
    // Smooth spinning vinyl angle that pauses cleanly in place when paused!
    val vinylRotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                val startAngle = vinylRotation.value % 360f
                vinylRotation.snapTo(startAngle)
                vinylRotation.animateTo(
                    targetValue = startAngle + 360f,
                    animationSpec = tween(durationMillis = 6500, easing = LinearEasing)
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "music_player_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPlaying) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_pulse"
    )
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_pulse"
    )
    val notesPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floating_notes"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, SoftGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SoftGold.copy(alpha = if (isPlaying) 0.20f else 0.10f),
                            PastelPurple.copy(alpha = if (isPlaying) 0.12f else 0.04f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Animated Spinning Vinyl + Glowing Ring + Floating Musical Notes
                Box(
                    modifier = Modifier.size(144.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing ambient aura rings while playing
                    if (isPlaying) {
                        Canvas(
                            modifier = Modifier
                                .size(138.dp)
                                .scale(auraScale)
                        ) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        SoftGold.copy(alpha = 0.28f),
                                        PastelBlue.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2f
                            )
                        }
                    }

                    // Track Progress Ring
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(120.dp),
                        color = SoftGold.copy(alpha = 0.2f),
                        strokeWidth = 5.dp,
                        trackColor = Color.Transparent
                    )
                    CircularProgressIndicator(
                        progress = { playbackProgress },
                        modifier = Modifier.size(120.dp),
                        color = SoftGold,
                        strokeWidth = 5.dp,
                        strokeCap = StrokeCap.Round,
                        trackColor = Color.Transparent
                    )

                    // Spinning Vinyl Disc with grooves & golden label highlight
                    Canvas(
                        modifier = Modifier
                            .size(102.dp)
                            .graphicsLayer { rotationZ = vinylRotation.value }
                    ) {
                        val r = size.minDimension / 2f
                        val c = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF23252E),
                                    Color(0xFF3E3B33),
                                    Color(0xFF1F2028),
                                    Color(0xFF474238),
                                    Color(0xFF23252E)
                                ),
                                center = c
                            ),
                            radius = r,
                            center = c
                        )
                        // Subtle vinyl grooves
                        listOf(0.86f, 0.74f, 0.62f).forEach { frac ->
                            drawCircle(
                                color = SoftGold.copy(alpha = 0.22f),
                                radius = r * frac,
                                center = c,
                                style = Stroke(width = 1.4f)
                            )
                        }
                        // Specular shine dot so rotation is unmistakably visible
                        drawCircle(
                            color = SoftGold.copy(alpha = 0.75f),
                            radius = 4.5f,
                            center = Offset(c.x, c.y - r * 0.78f)
                        )
                    }

                    // Center Cat Mascot Label
                    Surface(
                        modifier = Modifier
                            .size(62.dp)
                            .scale(pulseScale),
                        shape = CircleShape,
                        color = SoftGold.copy(alpha = 0.92f),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isPlaying) "🎧🐈‍⬛" else "🐈‍⬛",
                                fontSize = if (isPlaying) 24.sp else 28.sp
                            )
                        }
                    }

                    // Floating Musical Notes rising while playing
                    if (isPlaying) {
                        val noteSpecs = listOf(
                            Triple("🎵", -44f, 0f),
                            Triple("🎶", 44f, 0.35f),
                            Triple("✨", -26f, 0.68f)
                        )
                        noteSpecs.forEach { (symbol, baseX, delayPhase) ->
                            val p = (notesPhase + delayPhase) % 1f
                            val yOff = 18f - p * 82f
                            val xOff = baseX + sin(p * 2f * PI.toFloat()) * 10f
                            val alpha = sin(p * PI.toFloat()).coerceIn(0f, 1f)
                            Text(
                                text = symbol,
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationX = xOff
                                        translationY = yOff
                                        val s = 0.75f + 0.35f * alpha
                                        scaleX = s
                                        scaleY = s
                                    }
                                    .alpha(alpha)
                            )
                        }
                    }
                }

                // Live 7-bar Animated Equalizer Visualizer
                AudioEqualizerBars(
                    isPlaying = isPlaying,
                    barCount = 7,
                    modifier = Modifier.height(26.dp)
                )

                // Track Title & Time Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentTrack?.name ?: (if (isSerbia) "Izaberite muziku ili zvučni ambijent" else "Select music or sound ambience"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (currentTrack != null && trackDurationMs > 0) {
                        Text(
                            text = "${formatMillis(currentPositionMs)} / ${formatMillis(trackDurationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Animated Play/Pause Button
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .bouncyClickable(pressedScale = 0.88f) { onTogglePlayPause() },
                    shape = CircleShape,
                    color = SoftGold,
                    shadowElevation = if (isPlaying) 8.dp else 4.dp,
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.45f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn())
                                    .togetherWith(scaleOut() + fadeOut())
                            },
                            label = "play_pause_icon"
                        ) { playing ->
                            Text(
                                text = if (playing) "⏸️" else "▶️",
                                fontSize = 26.sp
                            )
                        }
                    }
                }

                // Volume Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = if (volume <= 0.01f) "🔇" else "🔊", fontSize = 20.sp)

                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = SoftGold,
                            activeTrackColor = SoftGold
                        )
                    )
                }

                // Sleep Timer Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "⏱️", fontSize = 18.sp)

                        val timerText = if (sleepTimerMinutes != null && remainingSeconds > 0) {
                            val mins = remainingSeconds / 60
                            val secs = remainingSeconds % 60
                            "Timer: $mins min $secs sec"
                        } else {
                            if (isSerbia) "Sleep Timer za spavanje ⏱️" else "Sleep Timer ⏱️"
                        }

                        Text(
                            text = timerText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15, 30, 45).forEach { min ->
                            val isSelected = sleepTimerMinutes == min
                            Surface(
                                modifier = Modifier.clickable {
                                    if (isSelected) onSetSleepTimer(null)
                                    else onSetSleepTimer(min)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SoftGold else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$min min",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BlackCharcoal else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (sleepTimerMinutes != null) {
                            Surface(
                                modifier = Modifier.clickable { onSetSleepTimer(null) },
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = if (isSerbia) "Isključi" else "Turn Off",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioEqualizerBars(
    isPlaying: Boolean,
    barCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val barDurations = listOf(420, 560, 380, 620, 480, 520, 400)
    val barColors = listOf(PastelBlue, PastelPurple, SoftGold, PastelBlue, PastelPurple, SoftGold, PastelBlue)

    val animatedFractions = List(barCount) { index ->
        val raw by infiniteTransition.animateFloat(
            initialValue = 0.22f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = barDurations[index % barDurations.size],
                    delayMillis = (index * 70) % 240,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "eq_bar_$index"
        )
        animateFloatAsState(
            targetValue = if (isPlaying) raw else 0.18f,
            animationSpec = tween(220),
            label = "eq_bar_settled_$index"
        ).value
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        animatedFractions.forEachIndexed { idx, frac ->
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height((24.dp * frac).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColors[idx % barColors.size])
            )
        }
    }
}

@Composable
fun TrackCard(
    track: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isSerbia = LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable(pressedScale = 0.97f) { onSelect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isCurrent) 2.dp else 1.dp,
            color = if (isCurrent) SoftGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = if (isCurrent) SoftGold else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = track.emoji, fontSize = 20.sp)
                    }
                }

                Column {
                    Text(
                        text = track.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (track.category) {
                            "LOFI" -> if (isSerbia) "Chill Lofi Muzika" else "Chill Lofi Music"
                            "AMBIENCE" -> if (isSerbia) "Zvučni Ambijent" else "Sound Ambience"
                            else -> if (isSerbia) "Moja MP3 Pesma" else "My MP3 Song"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPlaying) {
                    AudioEqualizerBars(
                        isPlaying = true,
                        barCount = 3,
                        modifier = Modifier
                            .height(20.dp)
                            .padding(end = 6.dp)
                    )
                }

                IconButton(onClick = {
                    coroutineScope.launch {
                        AudioShareHelper.shareAudioTrack(context, track)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (isPlaying) "⏸️" else "▶️",
                    fontSize = 22.sp
                )

                if (onDelete != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Obriši",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
