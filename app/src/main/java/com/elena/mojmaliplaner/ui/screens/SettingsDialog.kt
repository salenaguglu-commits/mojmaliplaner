package com.elena.mojmaliplaner.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elena.mojmaliplaner.R

@Composable
fun SettingsDialog(
    currentUserName: String,
    currentCatName: String,
    currentDogName: String,
    onSaveSettings: (userName: String, catName: String, dogName: String, newPin: String?) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: (Uri) -> Unit,
    onResetAllData: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var userName by remember { mutableStateOf(currentUserName) }
    var catName by remember { mutableStateOf(currentCatName) }
    var dogName by remember { mutableStateOf(currentDogName) }

    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    var showResetConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val backupPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onImportBackup(uri)
            Toast.makeText(context, context.getString(R.string.msg_restore_success), Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismissRequest) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Zatvori")
                    }
                }

                // 1. User Name
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text(stringResource(R.string.label_user_name)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Ime") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // 2. Pet Names
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text(stringResource(R.string.label_cat_name)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = dogName,
                        onValueChange = { dogName = it },
                        label = { Text(stringResource(R.string.label_dog_name)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                // 3. Change PIN
                Text(
                    text = stringResource(R.string.label_change_pin),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) newPin = it },
                    label = { Text(stringResource(R.string.label_new_pin)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) confirmPin = it },
                    label = { Text(stringResource(R.string.label_confirm_pin)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = {
                        val pinToUpdate = if (newPin.isNotBlank()) {
                            if (newPin.length == 4 && newPin == confirmPin) {
                                newPin
                            } else {
                                Toast.makeText(context, "Novi PIN mora imati 4 cifre i poklapati se!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        } else null

                        onSaveSettings(
                            userName.ifBlank { "Elena" },
                            catName.ifBlank { "Mia" },
                            dogName.ifBlank { "Bleki" },
                            pinToUpdate
                        )
                        Toast.makeText(context, "Podešavanja sačuvana! ✨", Toast.LENGTH_SHORT).show()
                        onDismissRequest()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.btn_save_settings), fontWeight = FontWeight.Bold)
                }

                // Backup & Restore Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportBackup,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.btn_export_backup), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { backupPickerLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.btn_import_backup), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                // About App Button
                OutlinedButton(
                    onClick = { showAboutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.btn_about_app), fontWeight = FontWeight.Bold)
                }

                // Reset All Button
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_reset_data), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutAppDialog(onDismiss = { showAboutDialog = false })
    }

    // Confirmation Dialog for Reset All
    if (showResetConfirm) {
        Dialog(onDismissRequest = { showResetConfirm = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
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
                    Text(
                        text = stringResource(R.string.reset_dialog_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = stringResource(R.string.reset_dialog_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showResetConfirm = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.btn_cancel))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showResetConfirm = false
                                onResetAllData()
                                onDismissRequest()
                                Toast.makeText(context, "Podaci su resetovani na fabričko stanje.", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.btn_reset_confirm))
                        }
                    }
                }
            }
        }
    }
}
