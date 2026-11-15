package com.elena.mojmaliplaner.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        PetLogEntity::class,
        DiaryEntryEntity::class,
        MusicSheetEntity::class,
        CustomTrackEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun petLogDao(): PetLogDao
    abstract fun diaryDao(): DiaryDao
    abstract fun musicSheetDao(): MusicSheetDao
    abstract fun customTrackDao(): CustomTrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moj_mali_planer_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
