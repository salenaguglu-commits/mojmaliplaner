package com.elena.mojmaliplaner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.db.TaskEntity
import com.elena.mojmaliplaner.ui.theme.SoftGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeTimeScreen(
    tasks: List<TaskEntity>,
    onAddTask: (title: String, category: String, colorHex: String, subjectName: String?) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onUpdateTask: (TaskEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (tasks.none { it.category == "SLOBODNO_IDEJA" }) {
            listOf(
                "Nacrtaj novu avanturu Mije i Blekija 🎨",
                "Napravi slagalicu ili lego kulu 🧱",
                "Pravi figurice od plastelina 🌈",
                "Nauči novu društvenu igru sa drugarima 🎲"
            ).forEach {
                onAddTask(it, "SLOBODNO_IDEJA", "#6EC5E9", null)
            }
        }

        if (tasks.none { it.category == "SLOBODNO_MEDA" }) {
            listOf(
                "Mali Princ" to "5",
                "Kralj Lavova" to "5",
                "Gospodar prstenova" to "5",
                "Hobit" to "5"
            ).forEach { (title, rating) ->
                onAddTask(title, "SLOBODNO_MEDA", "#C49ED4", rating)
            }
        }

        if (tasks.none { it.category == "SLOBODNO_ZELJA" }) {
            listOf(
                "Odlazak u prirodu sa Blekijem 🐕",
                "Pravljenje neke sjajne poslastice 🧁",
                "Vožnja bicikla 🚲",
                "Komponovanje nove kompozicije 🎼"
            ).forEach {
                onAddTask(it, "SLOBODNO_ZELJA", "#CBA84E", null)
            }
        }
    }

    var newIdeaInput by remember { mutableStateOf("") }
    var newMediaTitle by remember { mutableStateOf("") }
    var newWishInput by remember { mutableStateOf("") }

    val playIdeas = tasks.filter { it.category == "SLOBODNO_IDEJA" }
    val mediaList = tasks.filter { it.category == "SLOBODNO_MEDA" }
    val weekendWishes = tasks.filter { it.category == "SLOBODNO_ZELJA" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.free_time_topbar),
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
                    text = { Text(stringResource(R.string.tab_games_drawing), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text(stringResource(R.string.tab_books_movies), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text(stringResource(R.string.tab_weekend_wishes), fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTabIndex) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newIdeaInput,
                                    onValueChange = { newIdeaInput = it },
                                    placeholder = { Text("Dodaj novu ideju za igru...") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (newIdeaInput.isNotBlank()) {
                                            onAddTask(newIdeaInput, "SLOBODNO_IDEJA", "#6EC5E9", null)
                                            newIdeaInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj")
                                }
                            }
                        }

                        items(playIdeas, key = { it.id }) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Ideja",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(onClick = { onDeleteTask(task) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Obriši",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newMediaTitle,
                                    onValueChange = { newMediaTitle = it },
                                    placeholder = { Text("Naziv pročitane knjige ili filma...") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (newMediaTitle.isNotBlank()) {
                                            onAddTask(newMediaTitle, "SLOBODNO_MEDA", "#C49ED4", "5")
                                            newMediaTitle = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj")
                                }
                            }
                        }

                        items(mediaList, key = { it.id }) { task ->
                            val currentRating = (task.subjectName ?: "5").toIntOrNull() ?: 5

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            for (star in 1..5) {
                                                val isFilled = star <= currentRating
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Ocena $star",
                                                    tint = if (isFilled) SoftGold else MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clickable {
                                                            onUpdateTask(task.copy(subjectName = star.toString()))
                                                            Toast.makeText(context, "Ocena postavljena na $star ⭐", Toast.LENGTH_SHORT).show()
                                                        }
                                                )
                                            }
                                        }
                                    }

                                    IconButton(onClick = { onDeleteTask(task) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Obriši",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newWishInput,
                                    onValueChange = { newWishInput = it },
                                    placeholder = { Text("Dodaj želju za vikend...") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                Button(
                                    onClick = {
                                        if (newWishInput.isNotBlank()) {
                                            onAddTask(newWishInput, "SLOBODNO_ZELJA", "#CBA84E", null)
                                            newWishInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Dodaj")
                                }
                            }
                        }

                        items(weekendWishes, key = { it.id }) { task ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(1.dp, SoftGold)
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
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Želja",
                                            tint = SoftGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    IconButton(onClick = { onDeleteTask(task) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Obriši",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
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
