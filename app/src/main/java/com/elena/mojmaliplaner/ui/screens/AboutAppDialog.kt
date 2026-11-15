package com.elena.mojmaliplaner.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.CountryDetector
import com.elena.mojmaliplaner.ui.theme.SoftGold
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun AboutAppDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isSerbia = CountryDetector.isSerbiaUser(context)
    val qrBitmap = remember { generateNbsIpsQrCode() }
    
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "Nepoznato"

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.about_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.app_version, versionName),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Zatvori")
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.about_disclaimer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = stringResource(R.string.about_support),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                if (isSerbia && qrBitmap != null) {
                    Surface(
                        modifier = Modifier.size(160.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "IPS QR Kod",
                                modifier = Modifier.size(90.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "200 RSD",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = stringResource(R.string.ips_scan_instruction),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val url = "https://paypal.me/salenaguglu/2USD"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.btn_paypal),
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

private fun generateNbsIpsQrCode(): Bitmap? {
    return runCatching {
        val nbsPayload = "K:PR|V:01|C:1|R:105000000328269951|N:Aleksandar Stojanović PR Bloom.IT|I:RSD200,00|SF:289|S:Kafa za autora MojMaliPlaner"
        val sizePx = 512
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(nbsPayload, BarcodeFormat.QR_CODE, sizePx, sizePx)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                val isBlack = bitMatrix.get(x, y)
                bitmap.setPixel(
                    x,
                    y,
                    if (isBlack) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    }.getOrNull()
}
