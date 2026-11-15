package com.elena.mojmaliplaner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elena.mojmaliplaner.data.db.PetLogEntity
import com.elena.mojmaliplaner.ui.theme.PastelBlue
import com.elena.mojmaliplaner.ui.theme.PastelPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    catName: String,
    dogName: String,
    petLogs: List<PetLogEntity>,
    onLogPetAction: (petType: String, actionType: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedPetProfile by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kućni ljubimci 🐾",
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
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Brini o ljubimcima ($catName i $dogName) svaki dan! 🐱🐶",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Pet 1: Mačka
            item {
                PetTamagotchiCard(
                    petName = "Mačka $catName 🐈‍⬛",
                    petType = "MIA_MACKA",
                    petEmoji = "🐱",
                    petColor = PastelPurple,
                    actions = listOf("HRANA", "VODA", "CETKANJE", "MAZENJE"),
                    actionLabels = listOf("Nahrani 🍖", "Daj vodu 💧", "Očešljaj 🪮", "Pomazi 💖"),
                    petLogs = petLogs.filter { it.petType == "MIA_MACKA" },
                    onActionClick = { action ->
                        onLogPetAction("MIA_MACKA", action)
                        Toast.makeText(context, "$catName je srećna! ✨", Toast.LENGTH_SHORT).show()
                    },
                    onProfileClick = { selectedPetProfile = "MIA_MACKA" }
                )
            }

            // Pet 2: Pas
            item {
                PetTamagotchiCard(
                    petName = "Pas $dogName 🐕",
                    petType = "BLEKI_PAS",
                    petEmoji = "🐶",
                    petColor = PastelBlue,
                    actions = listOf("HRANA", "VODA", "SETNJA", "MAZENJE"),
                    actionLabels = listOf("Nahrani 🍖", "Daj vodu 💧", "U šetnju 🦮", "Igraj se 🎾"),
                    petLogs = petLogs.filter { it.petType == "BLEKI_PAS" },
                    onActionClick = { action ->
                        onLogPetAction("BLEKI_PAS", action)
                        Toast.makeText(context, "$dogName vrti repom! ✨", Toast.LENGTH_SHORT).show()
                    },
                    onProfileClick = { selectedPetProfile = "BLEKI_PAS" }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (selectedPetProfile != null) {
        val petName = if (selectedPetProfile == "MIA_MACKA") "Mačka Mia 🐈‍⬛" else "Pas Bleki 🐕"
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
                            text = "Profil: $petName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { selectedPetProfile = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Zatvori")
                        }
                    }

                    Text(
                        text = "Istorija brige i aktivnosti 📋",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (logs.isEmpty()) {
                        Text(
                            text = "Još nema zabeleženih aktivnosti za danas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            logs.take(5).forEach { log ->
                                val dateStr = SimpleDateFormat("HH:mm - dd.MM.", Locale("sr")).format(Date(log.timestampMillis))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Aktivnost: ${log.actionType}",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, petColor)
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
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = petColor.copy(alpha = 0.25f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = petEmoji, fontSize = 26.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = petName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Aktivnosti danas: ${petLogs.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = onProfileClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Profil", fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until actions.size step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val action1 = actions[i]
                        val label1 = actionLabels[i]

                        Button(
                            onClick = { onActionClick(action1) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = petColor,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(label1, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        if (i + 1 < actions.size) {
                            val action2 = actions[i + 1]
                            val label2 = actionLabels[i + 1]

                            Button(
                                onClick = { onActionClick(action2) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = petColor,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(label2, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
