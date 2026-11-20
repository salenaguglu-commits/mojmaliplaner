package com.elena.mojmaliplaner.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateMillis: Long,
    val category: String, // "ŠKOLA", "DNEVNI_PLAN", "OBAVEZA", "SLOBODNO_VREME"
    val colorHex: String,
    val isCompleted: Boolean = false,
    val subjectName: String? = null
)

@Entity(tableName = "pet_logs")
data class PetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petType: String, // "MIA_MACKA" ili "BLEKI_PAS"
    val actionType: String, // "HRANA", "VODA", "SETNJA", "CETKANJE", "MAZENJE"
    val timestampMillis: Long,
    val isDoneToday: Boolean = true
)

@Entity(tableName = "diary_entries")
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val title: String,
    val content: String,
    val moodEmoji: String, // 🐱, 😺, 😸, 🐾, ✨
    val imageUri: String? = null
)

@Entity(tableName = "music_sheets")
data class MusicSheetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val instrumentOrCategory: String, // "Muzička škola - Solfeđo", "Klavir / Note"
    val fileUri: String
)

@Entity(tableName = "custom_tracks")
data class CustomTrackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val category: String = "MOJA_MUZIKA"
)

@Entity(tableName = "unlocked_stickers")
data class UnlockedStickerEntity(
    @PrimaryKey val stickerId: Int,
    val unlockedAtMillis: Long,
    val isNew: Boolean = true
)
