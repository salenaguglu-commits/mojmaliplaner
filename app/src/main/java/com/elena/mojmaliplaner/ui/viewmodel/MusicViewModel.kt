package com.elena.mojmaliplaner.ui.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import androidx.annotation.RawRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.elena.mojmaliplaner.R
import com.elena.mojmaliplaner.data.db.CustomTrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class AudioTrack(
    val id: String,
    val name: String,
    val nameEn: String = name,
    @param:RawRes val resId: Int? = null,
    val filePath: String? = null,
    val category: String,
    val emoji: String,
    val isLooping: Boolean = true,
    val customTrackEntity: CustomTrackEntity? = null
) {
    fun displayName(isSerbia: Boolean): String = if (isSerbia) name else nameEn
}

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private var mediaPlayer: MediaPlayer? = null
    private var timerJob: Job? = null
    private var progressJob: Job? = null

    val factoryTracks = listOf(
        AudioTrack(id = "1", name = "Tokyo Vibe 🗼", nameEn = "Tokyo Vibe 🗼", resId = R.raw.tokyo_music, category = "LOFI", emoji = "🎧"),
        AudioTrack(id = "2", name = "Sonder Beat 🎶", nameEn = "Sonder Beat 🎶", resId = R.raw.sonder_beat, category = "LOFI", emoji = "🎵"),
        AudioTrack(id = "3", name = "Classic Hip-Hop 📻", nameEn = "Classic Hip-Hop 📻", resId = R.raw.musinova_classic_hip_hop, category = "LOFI", emoji = "🎼"),
        AudioTrack(id = "4", name = "Meka kiša 🌧️", nameEn = "Soft Rain 🌧️", resId = R.raw.calming_rain, category = "AMBIENCE", emoji = "🌧️"),
        AudioTrack(id = "5", name = "Macin glas 🐱", nameEn = "Cute Cat 🐱", resId = R.raw.cute_cat, category = "AMBIENCE", emoji = "😸", isLooping = false),
        AudioTrack(id = "6", name = "Mjaukanje 🐾", nameEn = "Cat Meow 🐾", resId = R.raw.cat_meow, category = "AMBIENCE", emoji = "🐈‍⬛", isLooping = false),
        AudioTrack(id = "7", name = "Bleki lavež 🐕", nameEn = "Blacky Bark 🐕", resId = R.raw.dragon_studio_dog_bark, category = "AMBIENCE", emoji = "🐶", isLooping = false)
    )

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _trackDurationMs = MutableStateFlow(1)
    val trackDurationMs: StateFlow<Int> = _trackDurationMs.asStateFlow()

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_isPlaying.value) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            _currentPositionMs.value = mp.currentPosition
                            _trackDurationMs.value = mp.duration.coerceAtLeast(1)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(200)
            }
        }
    }

    fun playTrack(track: AudioTrack) {
        if (_currentTrack.value?.id == track.id && mediaPlayer != null) {
            if (_isPlaying.value) {
                mediaPlayer?.pause()
                _isPlaying.value = false
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressLoop()
            }
            return
        }

        stopPlayback()

        try {
            if (track.resId != null) {
                mediaPlayer = MediaPlayer.create(getApplication(), track.resId)?.apply {
                    isLooping = track.isLooping
                    setVolume(_volume.value, _volume.value)
                    start()
                }
                _currentTrack.value = track
                _isPlaying.value = true
                startProgressLoop()
            } else if (!track.filePath.isNullOrBlank()) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(track.filePath)
                    setOnPreparedListener { mp ->
                        mp.isLooping = track.isLooping
                        mp.setVolume(_volume.value, _volume.value)
                        mp.start()
                        _currentTrack.value = track
                        _isPlaying.value = true
                        startProgressLoop()
                    }
                    prepareAsync()
                }
            }

            mediaPlayer?.setOnCompletionListener {
                if (!track.isLooping) {
                    _isPlaying.value = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (_isPlaying.value) {
                player.pause()
                _isPlaying.value = false
            } else {
                player.start()
                _isPlaying.value = true
                startProgressLoop()
            }
        } ?: run {
            factoryTracks.firstOrNull()?.let { playTrack(it) }
        }
    }

    fun setVolume(newVolume: Float) {
        _volume.value = newVolume
        mediaPlayer?.setVolume(newVolume, newVolume)
    }

    fun setSleepTimer(minutes: Int?) {
        timerJob?.cancel()
        _sleepTimerMinutes.value = minutes

        if (minutes == null) {
            _remainingSeconds.value = 0
            return
        }

        val totalSecs = minutes * 60
        _remainingSeconds.value = totalSecs

        timerJob = viewModelScope.launch {
            var currentSecs = totalSecs
            while (currentSecs > 0) {
                delay(1000)
                currentSecs--
                _remainingSeconds.value = currentSecs
            }
            stopPlayback()
            _sleepTimerMinutes.value = null
            _remainingSeconds.value = 0
        }
    }

    fun importAudioFile(uri: Uri, onFileSaved: (title: String, filePath: String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resolver = getApplication<Application>().contentResolver
                val dir = File(getApplication<Application>().filesDir, "custom_audio")
                if (!dir.exists()) dir.mkdirs()

                val isSerbia = getApplication<Application>().resources.configuration.locales[0].language.equals("sr", ignoreCase = true)
                var title = if (isSerbia) "Moja pesma" else "My Song"
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        title = cursor.getString(nameIndex).replace(".mp3", "", ignoreCase = true)
                    }
                }

                val destFile = File(dir, "custom_${System.currentTimeMillis()}.mp3")
                resolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (destFile.exists()) {
                    onFileSaved(title, destFile.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopPlayback() {
        progressJob?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        progressJob?.cancel()
        stopPlayback()
    }
}
