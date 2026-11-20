package com.elena.mojmaliplaner.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.elena.mojmaliplaner.ui.viewmodel.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AudioShareHelper {

    suspend fun shareAudioTrack(context: Context, track: AudioTrack) {
        val uri = getShareableUri(context, track) ?: return

        val isSerbia = context.resources.configuration.locales[0].language.equals("sr", ignoreCase = true)
        val title = track.displayName(isSerbia)
        val shareText = if (isSerbia) {
            "Evo jedna super pesma iz mog planera: $title \uD83C\uDFB5\u2728"
        } else {
            "Here is an awesome song from my planner: $title \uD83C\uDFB5\u2728"
        }
        val chooserTitle = if (isSerbia) "Podeli pesmu" else "Share song"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/mpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, title)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }

    private suspend fun getShareableUri(context: Context, track: AudioTrack): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                if (track.filePath != null) {
                    // Custom imported MP3 - just use FileProvider directly
                    val file = File(track.filePath)
                    if (file.exists()) {
                        return@withContext FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    }
                } else if (track.resId != null) {
                    // Built-in raw track - copy to cache and share
                    val sharedAudioDir = File(context.cacheDir, "shared_audio")
                    if (!sharedAudioDir.exists()) {
                        sharedAudioDir.mkdirs()
                    }
                    
                    val safeName = track.name.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                    val destFile = File(sharedAudioDir, "${safeName}.mp3")
                    
                    // Copy raw resource stream to the file
                    context.resources.openRawResource(track.resId).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    return@withContext FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        destFile
                    )
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
