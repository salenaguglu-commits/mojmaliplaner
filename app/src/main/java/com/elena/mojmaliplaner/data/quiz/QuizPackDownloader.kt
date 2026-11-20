package com.elena.mojmaliplaner.data.quiz

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class QuizPackMetadata(
    val packId: String,
    val language: String,
    val level: String,
    val titleSr: String,
    val titleEn: String,
    val questionCount: Int,
    val version: Int,
    val downloadUrl: String
)

object QuizPackDownloader {

    const val CATALOG_URL = "https://bloom-it.rs/planer-packs/catalog.json"
    private const val INSTALLED_PACKS_FILE = "installed_packs.json"

    suspend fun fetchCatalog(): List<QuizPackMetadata> = withContext(Dispatchers.IO) {
        val list = mutableListOf<QuizPackMetadata>()
        try {
            val url = URL(CATALOG_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(response)
                val packsArray = root.optJSONArray("packs") ?: JSONArray()
                
                for (i in 0 until packsArray.length()) {
                    val p = packsArray.getJSONObject(i)
                    list.add(
                        QuizPackMetadata(
                            packId = p.getString("packId"),
                            language = p.getString("language"),
                            level = p.getString("level"),
                            titleSr = p.getString("titleSr"),
                            titleEn = p.getString("titleEn"),
                            questionCount = p.getInt("questionCount"),
                            version = p.getInt("version"),
                            downloadUrl = p.getString("downloadUrl")
                        )
                    )
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun downloadPack(context: Context, pack: QuizPackMetadata): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(pack.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonContent = connection.inputStream.bufferedReader().use { it.readText() }
                
                // Validate if it's a valid JSON array or object with 'questions'
                val trimmed = jsonContent.trim()
                if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
                    val dir = File(context.filesDir, "quiz_packs")
                    if (!dir.exists()) dir.mkdirs()
                    
                    val file = File(dir, "pack_${pack.packId}.json")
                    file.writeText(jsonContent)
                    
                    updateInstalledPacksMetadata(context, pack)
                    return@withContext true
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }

    suspend fun getInstalledPacks(context: Context): List<QuizPackMetadata> = withContext(Dispatchers.IO) {
        val list = mutableListOf<QuizPackMetadata>()
        try {
            val dir = File(context.filesDir, "quiz_packs")
            val metaFile = File(dir, INSTALLED_PACKS_FILE)
            if (metaFile.exists()) {
                val array = JSONArray(metaFile.readText())
                for (i in 0 until array.length()) {
                    val p = array.getJSONObject(i)
                    list.add(
                        QuizPackMetadata(
                            packId = p.getString("packId"),
                            language = p.getString("language"),
                            level = p.getString("level"),
                            titleSr = p.getString("titleSr"),
                            titleEn = p.getString("titleEn"),
                            questionCount = p.getInt("questionCount"),
                            version = p.getInt("version"),
                            downloadUrl = p.getString("downloadUrl")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    private suspend fun updateInstalledPacksMetadata(context: Context, newPack: QuizPackMetadata) = withContext(Dispatchers.IO) {
        try {
            val currentList = getInstalledPacks(context).toMutableList()
            currentList.removeAll { it.packId == newPack.packId }
            currentList.add(newPack)

            val dir = File(context.filesDir, "quiz_packs")
            if (!dir.exists()) dir.mkdirs()
            val metaFile = File(dir, INSTALLED_PACKS_FILE)

            val array = JSONArray()
            currentList.forEach { p ->
                val obj = JSONObject().apply {
                    put("packId", p.packId)
                    put("language", p.language)
                    put("level", p.level)
                    put("titleSr", p.titleSr)
                    put("titleEn", p.titleEn)
                    put("questionCount", p.questionCount)
                    put("version", p.version)
                    put("downloadUrl", p.downloadUrl)
                }
                array.put(obj)
            }
            metaFile.writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun deletePack(context: Context, packId: String) = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.filesDir, "quiz_packs")
            val file = File(dir, "pack_$packId.json")
            if (file.exists()) {
                file.delete()
            }
            
            val currentList = getInstalledPacks(context).toMutableList()
            currentList.removeAll { it.packId == packId }
            val metaFile = File(dir, INSTALLED_PACKS_FILE)
            val array = JSONArray()
            currentList.forEach { p ->
                val obj = JSONObject().apply {
                    put("packId", p.packId)
                    put("language", p.language)
                    put("level", p.level)
                    put("titleSr", p.titleSr)
                    put("titleEn", p.titleEn)
                    put("questionCount", p.questionCount)
                    put("version", p.version)
                    put("downloadUrl", p.downloadUrl)
                }
                array.put(obj)
            }
            metaFile.writeText(array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
