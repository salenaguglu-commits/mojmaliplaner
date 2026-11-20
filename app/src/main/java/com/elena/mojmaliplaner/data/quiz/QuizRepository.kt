package com.elena.mojmaliplaner.data.quiz

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepository(private val context: Context) {

    suspend fun loadAllQuestions(): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val questionsMap = mutableMapOf<String, QuizQuestion>()

        // 1. Load starter quiz
        try {
            val starterJson = context.assets.open("starter_quiz.json").bufferedReader().use { it.readText() }
            parseQuizJson(starterJson).forEach { q ->
                questionsMap[q.id] = q
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Load custom packs
        try {
            val quizDir = File(context.filesDir, "quiz_packs")
            if (quizDir.exists()) {
                quizDir.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
                    try {
                        val packJson = file.readText()
                        parseQuizJson(packJson).forEach { q ->
                            questionsMap[q.id] = q
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        questionsMap.values.toList()
    }

    private fun parseQuizJson(jsonString: String): List<QuizQuestion> {
        val list = mutableListOf<QuizQuestion>()
        try {
            val jsonText = jsonString.trim()
            val array = if (jsonText.startsWith("[")) {
                JSONArray(jsonText)
            } else {
                val obj = JSONObject(jsonText)
                obj.optJSONArray("questions") ?: JSONArray()
            }

            for (i in 0 until array.length()) {
                val qObj = array.getJSONObject(i)
                val id = qObj.getString("id")

                val legacyOptions = parseJsonStringArray(qObj.optJSONArray("options"))
                val optionsSr = parseJsonStringArray(qObj.optJSONArray("optionsSr")).ifEmpty { legacyOptions }
                val optionsEn = parseJsonStringArray(qObj.optJSONArray("optionsEn")).ifEmpty { legacyOptions }

                val legacyCorrect = qObj.optString("correctAnswer", "")
                val correctAnswerSr = qObj.optString("correctAnswerSr", legacyCorrect).ifBlank { legacyCorrect }
                val correctAnswerEn = qObj.optString("correctAnswerEn", legacyCorrect).ifBlank { legacyCorrect }

                val legacyCategory = qObj.optString("category", "")
                val categorySr = qObj.optString("categorySr", legacyCategory).ifBlank { legacyCategory }
                val categoryEn = qObj.optString("categoryEn", legacyCategory).ifBlank { legacyCategory }

                list.add(
                    QuizQuestion(
                        id = id,
                        language = qObj.optString("language", "EN"),
                        level = qObj.optString("level", "A2"),
                        type = qObj.optString("type", "MULTIPLE_CHOICE"),
                        categorySr = categorySr,
                        categoryEn = categoryEn,
                        promptSr = qObj.optString("promptSr", ""),
                        promptEn = qObj.optString("promptEn", ""),
                        targetAudioText = qObj.optString("targetAudioText", ""),
                        optionsSr = optionsSr,
                        optionsEn = optionsEn,
                        correctAnswerSr = correctAnswerSr,
                        correctAnswerEn = correctAnswerEn,
                        explanationSr = qObj.optString("explanationSr", ""),
                        explanationEn = qObj.optString("explanationEn", "")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun parseJsonStringArray(array: JSONArray?): List<String> {
        val list = mutableListOf<String>()
        if (array != null) {
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        }
        return list
    }
}
