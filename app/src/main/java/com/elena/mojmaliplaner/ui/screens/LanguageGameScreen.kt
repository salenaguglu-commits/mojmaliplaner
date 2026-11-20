package com.elena.mojmaliplaner.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elena.mojmaliplaner.data.quiz.QuizQuestion
import com.elena.mojmaliplaner.data.quiz.QuizRepository
import com.elena.mojmaliplaner.ui.theme.PastelBlue
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

    // Local streak tracker for immediate UI celebration
    var localStreak by remember { mutableIntStateOf(languageStreak) }

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
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Section - Download button & Filters
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showPacksDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = if (isSerbia) "Preuzmi nove pakete ☁️" else "Download Packs ☁️", fontWeight = FontWeight.Bold)
                }

                // Language Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableLanguages) { lang ->
                        val count = if (lang == "ALL") playableQuestions.size else playableQuestions.count { it.language == lang }
                        val name = when(lang) {
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
                        val name = when(lvl) {
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isSerbia) "Nema pitanja za ovaj filter." else "No questions for this filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val q = currentQuestion!!
                val currentCategory = if (isSerbia) q.categorySr else q.categoryEn
                val currentOptions = if (isSerbia) q.optionsSr else q.optionsEn
                val currentCorrectAnswer = if (isSerbia) q.correctAnswerSr else q.correctAnswerEn

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, SoftGold)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Category & Audio
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = "$currentCategory - ${q.type}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            IconButton(onClick = { playAudio(q.targetAudioText, q.language) }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Izgovori", tint = MaterialTheme.colorScheme.primary)
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
                            // Typing Input Field
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
                            // Flashcard Flip View
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clickable { isFlashcardFlipped = !isFlashcardFlipped },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isFlashcardFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isFlashcardFlipped) currentCorrectAnswer else (if (isSerbia) "Dodirni da otkriješ odgovor 🔍" else "Tap to reveal answer 🔍"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFlashcardFlipped) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        } else {
                            // Multiple Choice
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                currentOptions.forEach { optionText ->
                                    val isSelected = selectedOption == optionText
                                    val isCorrect = optionText == currentCorrectAnswer

                                    val containerCol = when {
                                        !isAnswerRevealed && isSelected -> PastelBlue
                                        !isAnswerRevealed -> MaterialTheme.colorScheme.surfaceVariant
                                        isCorrect -> Color(0xFF81C784)
                                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !isAnswerRevealed) {
                                                selectedOption = optionText
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = containerCol)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                val expText = if (isSerbia) q.explanationSr else q.explanationEn
                                val showAnswerText = if (q.type == "TYPING" || currentOptions.isEmpty()) {
                                    "${if (isSerbia) "Tačan odgovor:" else "Correct answer:"} $currentCorrectAnswer\n$expText"
                                } else {
                                    expText
                                }

                                Surface(
                                    color = Color(0xFF81C784).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp),
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
                                            showStarCelebration = true
                                            localStreak = 0
                                        }
                                        onCorrectAnswer(q.id)
                                        playAudio(q.targetAudioText.ifBlank { currentCorrectAnswer }, q.language)
                                    } else {
                                        localStreak = 0
                                        onWrongAnswer()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color.Black)
                            ) {
                                Text(if (isSerbia) "Potvrdi" else "Confirm", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Gold Star Celebration Dialog
    if (showStarCelebration) {
        Dialog(onDismissRequest = { showStarCelebration = false }) {
            val infiniteTransition = rememberInfiniteTransition(label = "star_scale")
            val starScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, SoftGold)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 90.sp,
                        modifier = Modifier.scale(starScale)
                    )

                    Text(
                        text = if (isSerbia) "Bravo! 🔥 5 tačnih u nizu!" else "Awesome! 🔥 5 in a row!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
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
                        colors = ButtonDefaults.buttonColors(containerColor = SoftGold, contentColor = Color.Black),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSerbia) "Super! ✨" else "Awesome! ✨", fontWeight = FontWeight.Bold)
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
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
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
