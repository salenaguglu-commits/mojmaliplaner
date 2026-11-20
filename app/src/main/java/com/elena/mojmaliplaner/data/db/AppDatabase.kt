package com.elena.mojmaliplaner.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TaskEntity::class,
        PetLogEntity::class,
        DiaryEntryEntity::class,
        MusicSheetEntity::class,
        CustomTrackEntity::class,
        UnlockedStickerEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun petLogDao(): PetLogDao
    abstract fun diaryDao(): DiaryDao
    abstract fun musicSheetDao(): MusicSheetDao
    abstract fun customTrackDao(): CustomTrackDao
    abstract fun stickerDao(): StickerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `unlocked_stickers` (
                        `stickerId` INTEGER NOT NULL,
                        `unlockedAtMillis` INTEGER NOT NULL,
                        `isNew` INTEGER NOT NULL,
                        PRIMARY KEY(`stickerId`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moj_mali_planer_db"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
