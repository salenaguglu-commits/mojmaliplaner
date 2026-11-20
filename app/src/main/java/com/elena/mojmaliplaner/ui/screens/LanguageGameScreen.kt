package com.elena.mojmaliplaner.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.elena.mojmaliplaner.data.quiz.QuizQuestion
import com.elena.mojmaliplaner.data.quiz.QuizRepository
import com.elena.mojmaliplaner.ui.components.ConfettiBurstOverlay
import com.elena.mojmaliplaner.ui.components.SunburstGlow
import com.elena.mojmaliplaner.ui.components.bouncyClickable
import com.elena.mojmaliplaner.ui.components.shimmerSheen
import com.elena.mojmaliplaner.ui.theme.BlackCharcoal
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import com.elena.mojmaliplaner.ui.theme.SoftGold
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageGameScreen(
    languageStreak: Int,
    solvedQuestionIds: Set<String>,
    onCorrectAnswer: (questionId: String) -> Unit,
    onWrongAnswer: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val systemLocale = LocalConfiguration.current.locales[0]
    val isSerbia = systemLocale.language.equals("sr", ignoreCase = true)
    val sysLangCode = systemLocale.language.uppercase(Locale.ROOT)
    val quizRepo = remember { QuizRepository(context) }

    var allQuestions by remember { mutableStateOf<List<QuizQuestion>>(emptyList()) }
    var currentQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var selectedLanguage by remember { mutableStateOf("ALL") }
    var selectedLevel by remember { mutableStateOf("ALL") }
    var showPacksDialog by remember { mutableStateOf(false) }
    var showStarCelebration by remember { mutableStateOf(false) }
    var celebrationTrigger by remember { mutableIntStateOf(0) }
    var wrongShakeTrigger by remember { mutableIntStateOf(0) }

    // Local streak tracker for immediate UI celebration
    var localStreak by remember { mutableIntStateOf(languageStreak) }

    // Shake animation for wrong answer feedback
    val cardShakeOffset = remember { Animatable(0f) }
    LaunchedEffect(wrongShakeTrigger) {
        if (wrongShakeTrigger > 0) {
            listOf(-18f, 18f, -12f, 12f, -6f, 6f, 0f).forEach { target ->
                cardShakeOffset.animateTo(target, animationSpec = tween(42))
            }
        }
    }

    // TTS Setup
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Load Data
    LaunchedEffect(showPacksDialog) {
        if (!showPacksDialog) {
            val qList = quizRepo.loadAllQuestions()
            allQuestions = qList
        }
    }

    // Exclude native system language questions from quiz choices (e.g. if sysLang is EN, don't quiz EN!)
    val playableQuestions = allQuestions.filter { !it.language.equals(sysLangCode, ignoreCase = true) }

    // Filter Logic
    val availableLanguages = listOf("ALL") + playableQuestions.map { it.language }.distinct()
    val availableLevels = listOf("ALL", "A1", "A2", "B1")

    val filteredQuestions = playableQuestions.filter {
        (selectedLanguage == "ALL" || it.language == selectedLanguage) &&
            (selectedLevel == "ALL" || it.level == selectedLevel)
    }

    var selectedOption by remember { mutableStateOf<String?>(null) }
    var userTypedInput by remember { mutableStateOf("") }
    var isFlashcardFlipped by remember { mutableStateOf(false) }
    var isAnswerRevealed by remember { mutableStateOf(false) }
    val askedQuestionIds = remember { mutableSetOf<String>() }

    fun loadNextQuestion() {
        if (filteredQuestions.isNotEmpty()) {
            val unasked = filteredQuestions.filter { !askedQuestionIds.contains(it.id) }
            val nextQ = if (unasked.isNotEmpty()) {
                unasked.random()
            } else {
                askedQuestionIds.clear()
                filteredQuestions.random()
            }
            askedQuestionIds.add(nextQ.id)
            currentQuestion = nextQ
        } else {
            currentQuestion = null
        }
        selectedOption = null
        userTypedInput = ""
        isFlashcardFlipped = false
        isAnswerRevealed = false
    }

    // On Filter Change or data load, pick new question
    LaunchedEffect(selectedLanguage, selectedLevel, playableQuestions) {
        askedQuestionIds.clear()
        loadNextQuestion()
    }

    fun playAudio(text: String, langCode: String) {
        val locale = when (langCode) {
            "EN" -> Locale.UK
            "FR" -> Locale.FRANCE
            "ES" -> Locale("es", "ES")
            "DE" -> Locale.GERMANY
            "IT" -> Locale.ITALY
            else -> Locale.US
        }
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSerbia) "Jezička Igra 🌍" else "Language Game 🌍",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    val solvedCountInFilter = filteredQuestions.count { solvedQuestionIds.contains(it.id) }
                    val totalCountInFilter = filteredQuestions.size

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🎓", fontSize = 14.sp)
                            Text(
                                text = "$solvedCountInFilter/$totalCountInFilter",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PastelBlue.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, PastelBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🔥", fontSize = 14.sp)
                            Text(
                                text = "$localStreak/5",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Section - Download button & Filters
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showPacksDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = if (isSerbia) "Preuzmi nove pakete ☁️" else "Download Packs ☁️", fontWeight = FontWeight.Bold)
                }

                // Animated Streak Meter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { step ->
                        val active = step < localStreak
                        val dotScale by animateFloatAsState(
                            targetValue = if (active) 1.15f else 0.88f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "streak_dot_$step"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (active) SoftGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            animationSpec = tween(250),
                            label = "streak_color_$step"
                        )
                        Surface(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(26.dp)
                                .scale(dotScale),
                            shape = CircleShape,
                            color = dotColor,
                            border = BorderStroke(1.dp, if (active) SoftGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (active) "⭐" else "•",
                                    fontSize = if (active) 13.sp else 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Language Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableLanguages) { lang ->
                        val count = if (lang == "ALL") playableQuestions.size else playableQuestions.count { it.language == lang }
                        val name = when (lang) {
                            "ALL" -> if (isSerbia) "Miks 🌍" else "Mix 🌍"
                            "EN" -> if (isSerbia) "Engleski 🇬🇧" else "English 🇬🇧"
                            "FR" -> if (isSerbia) "Francuski 🇫🇷" else "French 🇫🇷"
                            "ES" -> if (isSerbia) "Španski 🇪🇸" else "Spanish 🇪🇸"
                            "DE" -> if (isSerbia) "Nemački 🇩🇪" else "German 🇩🇪"
                            "IT" -> if (isSerbia) "Italijanski 🇮🇹" else "Italian 🇮🇹"
                            else -> lang
                        }
                        val label = "$name ($count)"
                        FilterChip(
                            text = label,
                            isSelected = selectedLanguage == lang,
                            onClick = { selectedLanguage = lang }
                        )
                    }
                }

                // Level Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableLevels) { lvl ->
                        val langFiltered = if (selectedLanguage == "ALL") playableQuestions else playableQuestions.filter { it.language == selectedLanguage }
                        val count = if (lvl == "ALL") langFiltered.size else langFiltered.count { it.level == lvl }
                        val name = when (lvl) {
                            "ALL" -> if (isSerbia) "Svi nivoi" else "All Levels"
                            "A1" -> if (isSerbia) "Nivo 1 (A1)" else "Level 1 (A1)"
                            "A2" -> if (isSerbia) "Nivo 2 (A2)" else "Level 2 (A2)"
                            "B1" -> if (isSerbia) "Nivo 3 (B1)" else "Level 3 (B1)"
                            else -> lvl
                        }
                        val label = "$name ($count)"
                        FilterChip(
                            text = label,
                            isSelected = selectedLevel == lvl,
                            onClick = { selectedLevel = lvl }
                        )
                    }
                }
            }

            if (currentQuestion == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSerbia) "Nema pitanja za ovaj filter." else "No questions for this filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AnimatedContent(
                    targetState = currentQuestion!!,
                    transitionSpec = {
                        (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(260)))
                            .togetherWith(slideOutHorizontally(tween(240)) { -it / 3 } + fadeOut(tween(200)))
                    },
                    label = "question_card_transition"
                ) { q ->
                    val currentCategory = if (isSerbia) q.categorySr else q.categoryEn
                    val currentOptions = if (isSerbia) q.optionsSr else q.optionsEn
                    val currentCorrectAnswer = if (isSerbia) q.correctAnswerSr else q.correctAnswerEn

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer { translationX = cardShakeOffset.value },
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(2.dp, SoftGold),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            // Category & Audio
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp)) {
                                    Text(
                                        text = "$currentCategory • ${q.type}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                ) {
                                    IconButton(onClick = { playAudio(q.targetAudioText, q.language) }) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Izgovori",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = if (isSerbia) q.promptSr else q.promptEn,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )

                            // Question Input / Choices Section
                            if (q.type == "TYPING" || currentOptions.isEmpty()) {
                                OutlinedTextField(
                                    value = userTypedInput,
                                    onValueChange = { if (!isAnswerRevealed) userTypedInput = it },
                                    placeholder = { Text(if (isSerbia) "Upiši tvoj odgovor ovde..." else "Type your answer here...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    enabled = !isAnswerRevealed,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            } else if (q.type == "FLASHCARD") {
                                // True 3D Perspective Flip Flashcard
                                FlipFlashcard3D(
                                    isFlipped = isFlashcardFlipped,
                                    frontPrompt = if (isSerbia) "Dodirni karticu da okreneš 🔄" else "Tap card to flip 🔄",
                                    backAnswer = currentCorrectAnswer,
                                    backSubtitle = if (isSerbia) "Tačan prevod ✨" else "Translation ✨",
                                    onFlip = {
                                        isFlashcardFlipped = !isFlashcardFlipped
                                        if (isFlashcardFlipped) {
                                            playAudio(q.targetAudioText.ifBlank { currentCorrectAnswer }, q.language)
                                        }
                                    },
                                    onSpeak = {
                                        playAudio(q.targetAudioText.ifBlank { currentCorrectAnswer }, q.language)
                                    }
                                )
                            } else {
                                // Multiple Choice with smooth animated feedback
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    currentOptions.forEach { optionText ->
                                        val isSelected = selectedOption == optionText
                                        val isCorrect = optionText == currentCorrectAnswer

                                        val targetCol = when {
                                            !isAnswerRevealed && isSelected -> PastelBlue
                                            !isAnswerRevealed -> MaterialTheme.colorScheme.surfaceVariant
                                            isCorrect -> Color(0xFF81C784)
                                            isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        }
                                        val animatedCol by animateColorAsState(
                                            targetValue = targetCol,
                                            animationSpec = tween(250),
                                            label = "opt_col"
                                        )
                                        val optionScale by animateFloatAsState(
                                            targetValue = when {
                                                isAnswerRevealed && isCorrect -> 1.03f
                                                !isAnswerRevealed && isSelected -> 1.02f
                                                else -> 1f
                                            },
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                            label = "opt_scale"
                                        )

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .scale(optionScale)
                                                .bouncyClickable(enabled = !isAnswerRevealed, pressedScale = 0.97f) {
                                                    selectedOption = optionText
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = animatedCol),
                                            border = if (isSelected && !isAnswerRevealed) BorderStroke(1.5.dp, SoftGold) else null
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = optionText,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (!isAnswerRevealed && !isSelected) MaterialTheme.colorScheme.onSurfaceVariant else Color.Black
                                                )
                                                if (isAnswerRevealed) {
                                                    if (isCorrect) Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black)
                                                    else if (isSelected) Icon(Icons.Default.Close, contentDescription = null, tint = Color.Black)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Explanation section when answer is revealed
                            AnimatedVisibility(visible = isAnswerRevealed) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    val expText = if (isSerbia) q.explanationSr else q.explanationEn
                                    val showAnswerText = if (q.type == "TYPING" || currentOptions.isEmpty()) {
                                        "${if (isSerbia) "Tačan odgovor:" else "Correct answer:"} $currentCorrectAnswer\n$expText"
                                    } else {
                                        expText
                                    }

                                    Surface(
                                        color = Color(0xFF81C784).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, Color(0xFF81C784))
                                    ) {
                                        Text(
                                            text = showAnswerText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    Button(
                                        onClick = { loadNextQuestion() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(if (isSerbia) "Sledeće pitanje ➡️" else "Next question ➡️", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (!isAnswerRevealed) {
                                Button(
                                    onClick = {
                                        val isCorrect = when {
                                            q.type == "TYPING" || currentOptions.isEmpty() -> {
                                                if (userTypedInput.isBlank()) {
                                                    Toast.makeText(context, if (isSerbia) "Upiši odgovor prvo!" else "Type an answer first!", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                userTypedInput.trim().equals(currentCorrectAnswer.trim(), ignoreCase = true)
                                            }
                                            q.type == "FLASHCARD" -> {
                                                if (!isFlashcardFlipped) isFlashcardFlipped = true
                                                true
                                            }
                                            else -> {
                                                if (selectedOption == null) {
                                                    Toast.makeText(context, if (isSerbia) "Izaberi odgovor prvo!" else "Select an answer first!", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                selectedOption == currentCorrectAnswer
                                            }
                                        }

                                        isAnswerRevealed = true
                                        if (isCorrect) {
                                            localStreak++
                                            if (localStreak >= 5) {
                                                celebrationTrigger++
                                                showStarCelebration = true
                                                localStreak = 0
                                            }
                                            onCorrectAnswer(q.id)
                                            playAudio(q.targetAudioText.ifBlank { currentCorrectAnswer }, q.language)
                                        } else {
                                            wrongShakeTrigger++
                                            localStreak = 0
                                            onWrongAnswer()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = BlackCharcoal)
                                ) {
                                    Text(if (isSerbia) "Potvrdi" else "Confirm", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Gold Star Celebration Dialog with Full-Screen Confetti & Sunburst!
    if (showStarCelebration) {
        Dialog(
            onDismissRequest = { showStarCelebration = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val entranceScale = remember(celebrationTrigger) { Animatable(0.55f) }
            LaunchedEffect(celebrationTrigger) {
                entranceScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }

            val infiniteTransition = rememberInfiniteTransition(label = "star_scale")
            val starScale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(550, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ConfettiBurstOverlay(
                    triggerKey = celebrationTrigger,
                    particleCount = 96,
                    originYFraction = 0.4f
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .scale(entranceScale.value),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.5.dp, SoftGold),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SunburstGlow(color = SoftGold, modifier = Modifier.fillMaxSize())
                            Text(
                                text = "⭐",
                                fontSize = 84.sp,
                                modifier = Modifier.scale(starScale)
                            )
                        }

                        Text(
                            text = if (isSerbia) "Bravo! 🔥 5 tačnih u nizu!" else "Awesome! 🔥 5 in a row!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftGold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (isSerbia) "Osvojio/la si +1 Zlatnu Zvezdicu za Album Sličica! 🌟" else "You earned +1 Gold Star for your Sticker Album! 🌟",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = { showStarCelebration = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = BlackCharcoal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .shimmerSheen()
                        ) {
                            Text(if (isSerbia) "Super! ✨" else "Awesome! ✨", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showPacksDialog) {
        LanguagePacksDialog(onDismiss = { showPacksDialog = false })
    }
}

@Composable
private fun FlipFlashcard3D(
    isFlipped: Boolean,
    frontPrompt: String,
    backAnswer: String,
    backSubtitle: String,
    onFlip: () -> Unit,
    onSpeak: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow
        ),
        label = "flashcard_3d_rotation"
    )
    val isShowingBack = rotation > 90f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(2.dp, if (isShowingBack) SoftGold else PastelPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isShowingBack) {
                        Brush.linearGradient(
                            colors = listOf(
                                SoftGold.copy(alpha = 0.28f),
                                PastelBlue.copy(alpha = 0.25f)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                PastelPurple.copy(alpha = 0.35f),
                                PastelBlue.copy(alpha = 0.25f)
                            )
                        )
                    }
                )
                .shimmerSheen(
                    enabled = !isShowingBack,
                    shimmerColor = Color.White.copy(alpha = 0.28f),
                    durationMillis = 2600
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!isShowingBack) {
                // FRONT FACE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "✨ 🃏 ✨", fontSize = 26.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = frontPrompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // BACK FACE (counter-rotated by 180° so text is crisp and readable!)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SoftGold.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = backSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = backAnswer,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            shape = CircleShape,
                            color = SoftGold.copy(alpha = 0.28f),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { onSpeak() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Izgovori",
                                    tint = MaterialTheme.colorScheme.onSurface,
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

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(220),
        label = "chip_bg"
    )
    Surface(
        modifier = Modifier.bouncyClickable(pressedScale = 0.94f) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
