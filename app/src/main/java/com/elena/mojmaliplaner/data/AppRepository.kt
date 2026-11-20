package com.elena.mojmaliplaner.data

import android.content.Context
import com.elena.mojmaliplaner.data.db.AppDatabase
import com.elena.mojmaliplaner.data.db.CustomTrackEntity
import com.elena.mojmaliplaner.data.db.DiaryEntryEntity
import com.elena.mojmaliplaner.data.db.MusicSheetEntity
import com.elena.mojmaliplaner.data.db.PetLogEntity
import com.elena.mojmaliplaner.data.db.TaskEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val taskDao = db.taskDao()
    private val petLogDao = db.petLogDao()
    private val diaryDao = db.diaryDao()
    private val musicSheetDao = db.musicSheetDao()
    private val customTrackDao = db.customTrackDao()
    private val stickerDao = db.stickerDao()

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksForDate(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDate(startOfDayMillis, endOfDayMillis)
    }

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    // Pets
    val allPetLogs: Flow<List<PetLogEntity>> = petLogDao.getAllPetLogs()
    fun getPetLogs(petType: String): Flow<List<PetLogEntity>> = petLogDao.getLogsForPet(petType)
    suspend fun logPetAction(petType: String, actionType: String): Long {
        return petLogDao.insertLog(
            PetLogEntity(
                petType = petType,
                actionType = actionType,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun insertPetLog(log: PetLogEntity): Long = petLogDao.insertLog(log)

    // Diary
    val allDiaryEntries: Flow<List<DiaryEntryEntity>> = diaryDao.getAllDiaryEntries()
    suspend fun insertDiaryEntry(entry: DiaryEntryEntity): Long = diaryDao.insertDiaryEntry(entry)
    suspend fun deleteDiaryEntry(entry: DiaryEntryEntity) = diaryDao.deleteDiaryEntry(entry)

    // Music Sheets
    val allMusicSheets: Flow<List<MusicSheetEntity>> = musicSheetDao.getAllMusicSheets()
    suspend fun insertMusicSheet(sheet: MusicSheetEntity): Long = musicSheetDao.insertMusicSheet(sheet)
    suspend fun deleteMusicSheet(sheet: MusicSheetEntity) = musicSheetDao.deleteMusicSheet(sheet)

    // Custom Audio Tracks
    val allCustomTracks: Flow<List<CustomTrackEntity>> = customTrackDao.getAllCustomTracks()
    suspend fun insertCustomTrack(track: CustomTrackEntity): Long = customTrackDao.insertCustomTrack(track)
    suspend fun deleteCustomTrack(track: CustomTrackEntity) = customTrackDao.deleteCustomTrack(track)

    // Stickers
    val allUnlockedStickers: Flow<List<com.elena.mojmaliplaner.data.db.UnlockedStickerEntity>> = stickerDao.getAllUnlockedStickers()
    suspend fun insertSticker(sticker: com.elena.mojmaliplaner.data.db.UnlockedStickerEntity) = stickerDao.insertSticker(sticker)
    suspend fun updateSticker(sticker: com.elena.mojmaliplaner.data.db.UnlockedStickerEntity) = stickerDao.updateSticker(sticker)

    // Clear all tables for full factory reset
    suspend fun clearAllData() {
        db.clearAllTables()
    }
}
