package com.elena.mojmaliplaner.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elena.mojmaliplaner.data.quiz.QuizPackDownloader
import com.elena.mojmaliplaner.data.quiz.QuizPackMetadata
import kotlinx.coroutines.launch

@Composable
fun LanguagePacksDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var catalogPacks by remember { mutableStateOf<List<QuizPackMetadata>>(emptyList()) }
    var installedPacks by remember { mutableStateOf<List<QuizPackMetadata>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var downloadingPackId by remember { mutableStateOf<String?>(null) }
    
    val isSerbia = LocalConfiguration.current.locales[0].language.equals("sr", ignoreCase = true)

    LaunchedEffect(Unit) {
        installedPacks = QuizPackDownloader.getInstalledPacks(context)
        catalogPacks = QuizPackDownloader.fetchCatalog()
        isLoading = false
        
        if (catalogPacks.isEmpty()) {
            Toast.makeText(context, if (isSerbia) "Nema internet veze. Pokušaj ponovo kasnije." else "No internet connection. Try again later.", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSerbia) "Preuzmi nove pakete ☁️" else "Download Packs ☁️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zatvori")
                    }
                }

                val sysLangCode = LocalConfiguration.current.locales[0].language.uppercase(java.util.Locale.ROOT)
                val displayCatalogPacks = catalogPacks.filter { !it.language.equals(sysLangCode, ignoreCase = true) }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(20.dp))
                    }
                } else if (displayCatalogPacks.isEmpty()) {
                    Text(
                        text = if (isSerbia) "Trenutno nema dostupnih paketa ili nema interneta." else "No packs available or no internet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(400.dp)
                    ) {
                        items(displayCatalogPacks, key = { it.packId }) { pack ->
                            val installedMeta = installedPacks.find { it.packId == pack.packId }
                            val isInstalled = installedMeta != null
                            val isUpdateAvailable = isInstalled && (installedMeta?.version ?: 0) < pack.version
                            val isDownloading = downloadingPackId == pack.packId

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text = if (isSerbia) pack.titleSr else pack.titleEn,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${pack.language} ${pack.level}",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${pack.questionCount} ${if (isSerbia) "pitanja" else "questions"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isInstalled) {
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        QuizPackDownloader.deletePack(context, pack.packId)
                                                        installedPacks = QuizPackDownloader.getInstalledPacks(context)
                                                        Toast.makeText(context, if (isSerbia) "Paket obrisan." else "Pack deleted.", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, tint = MaterialTheme.colorScheme.error, contentDescription = "Obriši")
                                            }
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        if (isDownloading) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                        } else if (isUpdateAvailable) {
                                            Button(
                                                onClick = {
                                                    downloadingPackId = pack.packId
                                                    coroutineScope.launch {
                                                        val success = QuizPackDownloader.downloadPack(context, pack)
                                                        downloadingPackId = null
                                                        if (success) {
                                                            installedPacks = QuizPackDownloader.getInstalledPacks(context)
                                                            Toast.makeText(context, if (isSerbia) "Paket ažuriran!" else "Pack updated!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, if (isSerbia) "Greška pri preuzimanju." else "Download error.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                            ) {
                                                Text(if (isSerbia) "Ažuriraj 🔄" else "Update 🔄")
                                            }
                                        } else if (isInstalled) {
                                            OutlinedButton(onClick = { }, enabled = false) {
                                                Text(if (isSerbia) "Preuzeto ✅" else "Downloaded ✅")
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    downloadingPackId = pack.packId
                                                    coroutineScope.launch {
                                                        val success = QuizPackDownloader.downloadPack(context, pack)
                                                        downloadingPackId = null
                                                        if (success) {
                                                            installedPacks = QuizPackDownloader.getInstalledPacks(context)
                                                            Toast.makeText(context, if (isSerbia) "Paket preuzet!" else "Pack downloaded!", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(context, if (isSerbia) "Greška pri preuzimanju." else "Download error.", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            ) {
                                                Text(if (isSerbia) "Preuzmi 📥" else "Download 📥")
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
