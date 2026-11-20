package com.elena.mojmaliplaner.data.quiz

data class QuizQuestion(
    val id: String,
    val language: String,
    val level: String,
    val type: String,
    val categorySr: String,
    val categoryEn: String,
    val promptSr: String,
    val promptEn: String,
    val targetAudioText: String,
    val optionsSr: List<String>,
    val optionsEn: List<String>,
    val correctAnswerSr: String,
    val correctAnswerEn: String,
    val explanationSr: String,
    val explanationEn: String
)
