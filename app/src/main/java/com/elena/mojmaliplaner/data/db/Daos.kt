package com.elena.mojmaliplaner.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dateMillis ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dateMillis >= :startOfDayMillis AND dateMillis < :endOfDayMillis")
    fun getTasksForDate(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}

@Dao
interface PetLogDao {
    @Query("SELECT * FROM pet_logs ORDER BY timestampMillis DESC")
    fun getAllPetLogs(): Flow<List<PetLogEntity>>

    @Query("SELECT * FROM pet_logs WHERE petType = :petType ORDER BY timestampMillis DESC")
    fun getLogsForPet(petType: String): Flow<List<PetLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PetLogEntity): Long
}

@Dao
interface DiaryDao {
    @Query("SELECT * FROM diary_entries ORDER BY dateMillis DESC")
    fun getAllDiaryEntries(): Flow<List<DiaryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntryEntity): Long

    @Delete
    suspend fun deleteDiaryEntry(entry: DiaryEntryEntity)
}

@Dao
interface MusicSheetDao {
    @Query("SELECT * FROM music_sheets ORDER BY id DESC")
    fun getAllMusicSheets(): Flow<List<MusicSheetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusicSheet(sheet: MusicSheetEntity): Long

    @Delete
    suspend fun deleteMusicSheet(sheet: MusicSheetEntity)
}

@Dao
interface CustomTrackDao {
    @Query("SELECT * FROM custom_tracks ORDER BY id DESC")
    fun getAllCustomTracks(): Flow<List<CustomTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTrack(track: CustomTrackEntity): Long

    @Delete
    suspend fun deleteCustomTrack(track: CustomTrackEntity)
}
