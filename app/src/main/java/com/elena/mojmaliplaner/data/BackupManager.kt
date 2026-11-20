package com.elena.mojmaliplaner.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.elena.mojmaliplaner.data.db.DiaryEntryEntity
import com.elena.mojmaliplaner.data.db.MusicSheetEntity
import com.elena.mojmaliplaner.data.db.PetLogEntity
import com.elena.mojmaliplaner.data.db.TaskEntity
import com.elena.mojmaliplaner.data.db.UnlockedStickerEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    fun createBackupJson(
        userName: String,
        catName: String,
        dogName: String,
        userPin: String,
        bronzeStars: Int,
        silverStars: Int,
        availableStars: Int,
        totalStars: Int,
        solvedQuestionIds: Set<String>,
        tasks: List<TaskEntity>,
        petLogs: List<PetLogEntity>,
        diaryEntries: List<DiaryEntryEntity>,
        musicSheets: List<MusicSheetEntity>,
        unlockedStickers: List<UnlockedStickerEntity>
    ): String {
        val root = JSONObject()

        val prefs = JSONObject().apply {
            put("userName", userName)
            put("catName", catName)
            put("dogName", dogName)
            put("userPin", userPin)
            put("bronzeStars", bronzeStars)
            put("silverStars", silverStars)
            put("availableStars", availableStars)
            put("totalStars", totalStars)

            val solvedArray = JSONArray()
            solvedQuestionIds.forEach { solvedArray.put(it) }
            put("solvedQuestionIds", solvedArray)
        }
        root.put("preferences", prefs)

        // Tasks
        val tasksArray = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("dateMillis", t.dateMillis)
                put("category", t.category)
                put("colorHex", t.colorHex)
                put("isCompleted", t.isCompleted)
                put("subjectName", t.subjectName ?: "")
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Pet Logs
        val petLogsArray = JSONArray()
        petLogs.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("petType", p.petType)
                put("actionType", p.actionType)
                put("timestampMillis", p.timestampMillis)
                put("isDoneToday", p.isDoneToday)
            }
            petLogsArray.put(obj)
        }
        root.put("petLogs", petLogsArray)

        // Diary
        val diaryArray = JSONArray()
        diaryEntries.forEach { d ->
            val obj = JSONObject().apply {
                put("id", d.id)
                put("dateMillis", d.dateMillis)
                put("title", d.title)
                put("content", d.content)
                put("moodEmoji", d.moodEmoji)
                put("imageUri", d.imageUri ?: "")
            }
            diaryArray.put(obj)
        }
        root.put("diaryEntries", diaryArray)

        // Music Sheets
        val musicArray = JSONArray()
        musicSheets.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("instrumentOrCategory", m.instrumentOrCategory)
                put("fileUri", m.fileUri)
            }
            musicArray.put(obj)
        }
        root.put("musicSheets", musicArray)

        // Stickers
        val stickersArray = JSONArray()
        unlockedStickers.forEach { s ->
            val obj = JSONObject().apply {
                put("stickerId", s.stickerId)
                put("unlockedAtMillis", s.unlockedAtMillis)
                put("isNew", s.isNew)
            }
            stickersArray.put(obj)
        }
        root.put("unlockedStickers", stickersArray)

        return root.toString(2)
    }

    fun exportAndShareBackup(context: Context, jsonString: String) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "MojMaliPlaner_Backup_$timeStamp.json"
            val backupFile = File(context.cacheDir, fileName)
            backupFile.writeText(jsonString)

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Sačuvaj rezervnu kopiju (Backup)"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class ParsedBackup(
        val userName: String?,
        val catName: String?,
        val dogName: String?,
        val userPin: String?,
        val bronzeStars: Int,
        val silverStars: Int,
        val availableStars: Int,
        val totalStars: Int,
        val solvedQuestionIds: Set<String>,
        val tasks: List<TaskEntity>,
        val petLogs: List<PetLogEntity>,
        val diaryEntries: List<DiaryEntryEntity>,
        val musicSheets: List<MusicSheetEntity>,
        val unlockedStickers: List<UnlockedStickerEntity>
    )

    fun parseBackupJson(jsonString: String): ParsedBackup? {
        return try {
            val root = JSONObject(jsonString)

            val prefs = root.optJSONObject("preferences")
            val userName = prefs?.optString("userName")
            val catName = prefs?.optString("catName")
            val dogName = prefs?.optString("dogName")
            val userPin = prefs?.optString("userPin")
            val bronzeStars = prefs?.optInt("bronzeStars", 0) ?: 0
            val silverStars = prefs?.optInt("silverStars", 0) ?: 0
            val availableStars = prefs?.optInt("availableStars", 0) ?: 0
            val totalStars = prefs?.optInt("totalStars", 0) ?: 0

            val solvedSet = mutableSetOf<String>()
            val solvedArray = prefs?.optJSONArray("solvedQuestionIds")
            if (solvedArray != null) {
                for (i in 0 until solvedArray.length()) {
                    solvedSet.add(solvedArray.getString(i))
                }
            }

            // Tasks
            val tasksList = mutableListOf<TaskEntity>()
            val tasksArray = root.optJSONArray("tasks")
            if (tasksArray != null) {
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    tasksList.add(
                        TaskEntity(
                            title = obj.getString("title"),
                            dateMillis = obj.getLong("dateMillis"),
                            category = obj.getString("category"),
                            colorHex = obj.getString("colorHex"),
                            isCompleted = obj.getBoolean("isCompleted"),
                            subjectName = obj.optString("subjectName").ifEmpty { null }
                        )
                    )
                }
            }

            // Pet Logs
            val petLogsList = mutableListOf<PetLogEntity>()
            val petLogsArray = root.optJSONArray("petLogs")
            if (petLogsArray != null) {
                for (i in 0 until petLogsArray.length()) {
                    val obj = petLogsArray.getJSONObject(i)
                    petLogsList.add(
                        PetLogEntity(
                            petType = obj.getString("petType"),
                            actionType = obj.getString("actionType"),
                            timestampMillis = obj.getLong("timestampMillis"),
                            isDoneToday = obj.optBoolean("isDoneToday", true)
                        )
                    )
                }
            }

            // Diary
            val diaryList = mutableListOf<DiaryEntryEntity>()
            val diaryArray = root.optJSONArray("diaryEntries")
            if (diaryArray != null) {
                for (i in 0 until diaryArray.length()) {
                    val obj = diaryArray.getJSONObject(i)
                    diaryList.add(
                        DiaryEntryEntity(
                            dateMillis = obj.getLong("dateMillis"),
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            moodEmoji = obj.getString("moodEmoji"),
                            imageUri = obj.optString("imageUri", null)
                        )
                    )
                }
            }

            // Music Sheets
            val musicList = mutableListOf<MusicSheetEntity>()
            val musicArray = root.optJSONArray("musicSheets")
            if (musicArray != null) {
                for (i in 0 until musicArray.length()) {
                    val obj = musicArray.getJSONObject(i)
                    musicList.add(
                        MusicSheetEntity(
                            title = obj.getString("title"),
                            instrumentOrCategory = obj.getString("instrumentOrCategory"),
                            fileUri = obj.getString("fileUri")
                        )
                    )
                }
            }

            // Stickers
            val stickersList = mutableListOf<UnlockedStickerEntity>()
            val stickersArray = root.optJSONArray("unlockedStickers")
            if (stickersArray != null) {
                for (i in 0 until stickersArray.length()) {
                    val obj = stickersArray.getJSONObject(i)
                    stickersList.add(
                        UnlockedStickerEntity(
                            stickerId = obj.getInt("stickerId"),
                            unlockedAtMillis = obj.getLong("unlockedAtMillis"),
                            isNew = obj.optBoolean("isNew", false)
                        )
                    )
                }
            }

            ParsedBackup(
                userName = userName,
                catName = catName,
                dogName = dogName,
                userPin = userPin,
                bronzeStars = bronzeStars,
                silverStars = silverStars,
                availableStars = availableStars,
                totalStars = totalStars,
                solvedQuestionIds = solvedSet,
                tasks = tasksList,
                petLogs = petLogsList,
                diaryEntries = diaryList,
                musicSheets = musicList,
                unlockedStickers = stickersList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
