// edit_minigame.kt
package com.example.indokuistampilan2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.indokuistampilan2.model.Question
import org.json.JSONException

class edit_minigame : AppCompatActivity() {
    companion object {
        private const val TAG = "EditMinigame"
        private const val BASE_URL = "http://192.168.8.137/indokuis"
        private const val MAX_QUESTIONS = 5
    }

    private var topicId = 0
    private var currentIndex = 1
    private var timerValue: String = ""
    private val questions = ArrayList<Question>()
    private var correctOption = ""

    private lateinit var tvTimer: TextView
    private lateinit var tvNumber: TextView
    private lateinit var etQuestion: EditText
    private lateinit var etA: EditText
    private lateinit var etB: EditText
    private lateinit var etC: EditText
    private lateinit var etD: EditText
    private lateinit var btnNext: Button
    private lateinit var backButton: ImageButton
    private lateinit var answerAButton: ImageButton
    private lateinit var answerBButton: ImageButton
    private lateinit var answerCButton: ImageButton
    private lateinit var answerDButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_minigame)

        topicId = intent.getIntExtra("topic_id", 0)
        if (topicId == 0) {
            Toast.makeText(this, "Invalid Topic ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvTimer       = findViewById(R.id.tvTimer)
        tvNumber      = findViewById(R.id.tvQuestionNumber)
        etQuestion    = findViewById(R.id.etQuestion)
        etA           = findViewById(R.id.etOptionA)
        etB           = findViewById(R.id.etOptionB)
        etC           = findViewById(R.id.etOptionC)
        etD           = findViewById(R.id.etOptionD)
        answerAButton = findViewById(R.id.answerAButton)
        answerBButton = findViewById(R.id.answerBButton)
        answerCButton = findViewById(R.id.answerCButton)
        answerDButton = findViewById(R.id.answerDButton)
        btnNext       = findViewById(R.id.btn_next)
        backButton    = findViewById(R.id.btn_back)

        answerAButton.setOnClickListener { selectAnswer("A") }
        answerBButton.setOnClickListener { selectAnswer("B") }
        answerCButton.setOnClickListener { selectAnswer("C") }
        answerDButton.setOnClickListener { selectAnswer("D") }

        btnNext.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            saveCurrent()
            // Use question index logic: if current is less than max, advance; if equal, redirect
            if (currentIndex < MAX_QUESTIONS) {
                currentIndex++
                updateNumber()
                clearFields()
                prefillOrClear(currentIndex)
            } else if (currentIndex == MAX_QUESTIONS) {
                // Redirect on 5th question
                Intent(this, edit_minigame2::class.java).apply {
                    putExtra("topic_id", topicId)
                    putExtra("timer", timerValue)
                    putParcelableArrayListExtra("questions", questions)
                    startActivity(this)
                }
                finish()
            }
        }
        backButton.setOnClickListener { onBackPressed() }

        loadMinigameTimer(topicId)
    }

    private fun loadMinigameTimer(id: Int) {
        val url = "$BASE_URL/get_minigame_topics.php?id=$id"
        Log.d(TAG, "Loading minigame timer from: $url")
        Volley.newRequestQueue(this).add(
            JsonObjectRequest(Request.Method.GET, url, null,
                { resp ->
                    Log.d(TAG, "Raw JSON (minigame): $resp")
                    if (resp.optString("status") == "success") {
                        try {
                            val m = resp.getJSONObject("minigame")
                            val dur = m.optInt("durasi_minigame", 0)
                            timerValue = dur.toString()
                            tvTimer.text = timerValue
                            Log.d(TAG, "Parsed minigame timer: $timerValue")
                        } catch (e: JSONException) {
                            Log.e(TAG, "Minigame timer parse error", e)
                            tvTimer.text = "0"
                        }
                    }
                    loadExistingQuestions()
                    updateNumber()
                    prefillOrClear(1)
                },
                { error ->
                    Log.e(TAG, "Volley error loading minigame timer", error)
                    loadExistingQuestions()
                    updateNumber()
                    prefillOrClear(1)
                }
            )
        )
    }

    private fun loadExistingQuestions() {
        val url = "$BASE_URL/get_minigame_questions.php?topic_id=$topicId"
        Volley.newRequestQueue(this).add(
            JsonArrayRequest(Request.Method.GET, url, null,
                { arr ->
                    questions.clear()
                    val count = arr.length().coerceAtMost(MAX_QUESTIONS)
                    for (i in 0 until count) {
                        try {
                            val obj = arr.getJSONObject(i)
                            questions.add(
                                Question(
                                    questionText  = obj.getString("question_text"),
                                    answerA       = obj.getString("option_a"),
                                    answerB       = obj.getString("option_b"),
                                    answerC       = obj.getString("option_c"),
                                    answerD       = obj.getString("option_d"),
                                    correctAnswer = obj.getString("correct_option"),
                                    timer         = timerValue
                                )
                            )
                        } catch (e: JSONException) {
                            Log.e(TAG, "Parse error Q$i", e)
                        }
                    }
                },
                { error -> Log.e(TAG, "Volley error loading questions", error) }
            )
        )
    }

    private fun prefillOrClear(index: Int) {
        if (questions.size >= index) {
            val q = questions[index - 1]
            etQuestion.setText(q.questionText)
            etA.setText(q.answerA)
            etB.setText(q.answerB)
            etC.setText(q.answerC)
            etD.setText(q.answerD)
            selectAnswer(q.correctAnswer)
        } else {
            clearFields()
        }
    }

    private fun updateNumber() {
        tvNumber.text = "Question $currentIndex / $MAX_QUESTIONS"
    }

    private fun clearFields() {
        etQuestion.text.clear()
        etA.text.clear()
        etB.text.clear()
        etC.text.clear()
        etD.text.clear()
        correctOption = ""
        resetAnswerSelection()
    }

    private fun validateInputs(): Boolean {
        if (etQuestion.text.isBlank() ||
            etA.text.isBlank()        ||
            etB.text.isBlank()        ||
            etC.text.isBlank()        ||
            etD.text.isBlank()        ||
            correctOption.isEmpty()
        ) {
            Toast.makeText(this, "Fill all fields and choose correct option", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveCurrent() {
        val q = Question(
            questionText  = etQuestion.text.toString(),
            answerA       = etA.text.toString(),
            answerB       = etB.text.toString(),
            answerC       = etC.text.toString(),
            answerD       = etD.text.toString(),
            correctAnswer = correctOption,
            timer         = timerValue
        )
        if (questions.size >= currentIndex) {
            questions[currentIndex - 1] = q
        } else {
            questions.add(q)
        }
    }

    private fun selectAnswer(ans: String) {
        resetAnswerSelection()
        correctOption = ans
        when (ans) {
            "A" -> answerAButton.setBackgroundResource(R.drawable.check_image)
            "B" -> answerBButton.setBackgroundResource(R.drawable.check_image)
            "C" -> answerCButton.setBackgroundResource(R.drawable.check_image)
            "D" -> answerDButton.setBackgroundResource(R.drawable.check_image)
        }
    }

    private fun resetAnswerSelection() {
        answerAButton.setBackgroundResource(R.drawable.answer_edit)
        answerBButton.setBackgroundResource(R.drawable.answer_edit)
        answerCButton.setBackgroundResource(R.drawable.answer_edit)
        answerDButton.setBackgroundResource(R.drawable.answer_edit)
    }
}
