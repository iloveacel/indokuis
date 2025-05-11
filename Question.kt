package com.example.indokuistampilan2.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Question(
    val questionText: String,
    val answerA: String,
    val answerB: String,
    val answerC: String,
    val answerD: String,
    val correctAnswer: String,
    val timer: String
) : Parcelable
