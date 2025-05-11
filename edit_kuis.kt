package com.example.indokuistampilan2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.indokuistampilan2.model.Question
import org.json.JSONException
import org.json.JSONObject

class edit_kuis : AppCompatActivity() {

    private lateinit var edtQuestion: EditText
    private lateinit var edtAnswerA: EditText
    private lateinit var edtAnswerB: EditText
    private lateinit var edtAnswerC: EditText
    private lateinit var edtAnswerD: EditText
    private lateinit var tvTimer: TextView
    private lateinit var tvQuestionNumber: TextView

    private lateinit var answerAButton: ImageButton
    private lateinit var answerBButton: ImageButton
    private lateinit var answerCButton: ImageButton
    private lateinit var answerDButton: ImageButton

    private var currentQuestionIndex = 1
    private var topicId: Int = 0
    private var correctAnswer: String = ""
    private var timerValue: String = "0"

    private val questionsList = ArrayList<Question>()

    companion object {
        private const val TAG = "EditKuis"
        private const val BASE_URL = "http://192.168.8.137/indokuis"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_kuis)

        // Bind views
        edtQuestion      = findViewById(R.id.questionBox)
        edtAnswerA       = findViewById(R.id.answerA)
        edtAnswerB       = findViewById(R.id.answerB)
        edtAnswerC       = findViewById(R.id.answerC)
        edtAnswerD       = findViewById(R.id.answerD)
        tvTimer          = findViewById(R.id.timer)
        tvQuestionNumber = findViewById(R.id.questionNumber)
        answerAButton    = findViewById(R.id.answerAButton)
        answerBButton    = findViewById(R.id.answerBButton)
        answerCButton    = findViewById(R.id.answerCButton)
        answerDButton    = findViewById(R.id.answerDButton)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        // Get topic ID
        topicId = intent.getIntExtra("topic_id", 0)
        if (topicId == 0) {
            Toast.makeText(this, "Invalid Topic ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load timer first, then questions
        loadQuizTimer(topicId)

        // Answer selection
        answerAButton.setOnClickListener { selectAnswer("A") }
        answerBButton.setOnClickListener { selectAnswer("B") }
        answerCButton.setOnClickListener { selectAnswer("C") }
        answerDButton.setOnClickListener { selectAnswer("D") }

        findViewById<Button>(R.id.btn_next).setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            saveCurrent()
            currentQuestionIndex++
            if (currentQuestionIndex <= 9) {
                updateQuestionNumber()
                prefillOrClear(currentQuestionIndex)
            } else {
                Intent(this, edit_kuis2::class.java).apply {
                    putExtra("topic_id", topicId)
                    putExtra("timer", timerValue)
                    putParcelableArrayListExtra("questions_list", questionsList)
                }.also {
                    startActivity(it)
                    finish()
                }
            }
        }
    }

    private fun loadQuizTimer(topicId: Int) {
        val url = "$BASE_URL/get_quiz_topics.php?id=$topicId"
        Log.d(TAG, "Loading timer from: $url")
        Volley.newRequestQueue(this).add(JsonObjectRequest(Request.Method.GET, url, null,
            { resp ->
                Log.d(TAG, "Raw JSON response: $resp")
                if (resp.optString("status") == "success") {
                    try {
                        // 1) Grab the "quiz" object
                        val q = resp.getJSONObject("quiz")
                        // 2) Read the exact field name from your JSON: "durasi_per_soal"
                        val dur = q.optInt("durasi_per_soal", 0)
                        timerValue = dur.toString()
                        tvTimer.text = timerValue
                        Log.d(TAG, "Parsed quiz timer: $timerValue")
                    } catch (e: JSONException) {
                        Log.e(TAG, "Timer parse error", e)
                    }
                } else {
                    Log.w(TAG, "Server returned error: ${resp.optString("message")}")
                }
                // 3) Now that timerValue is set correctly, load the questions
                loadExistingQuestions()
            },
            { error ->
                Log.e(TAG, "Timer load error", error)
                loadExistingQuestions()
            }
        ))
    }




    private fun loadExistingQuestions() {
        val url = "$BASE_URL/get_quiz_questions.php?topic_id=$topicId"
        Volley.newRequestQueue(this).add(JsonArrayRequest(Request.Method.GET, url, null,
            { arr ->
                val count = arr.length().coerceAtMost(10)
                for (i in 0 until count) {
                    val obj = arr.getJSONObject(i)
                    questionsList.add(
                        Question(
                            questionText  = obj.getString("question_text"),
                            answerA       = obj.getString("answer_a"),
                            answerB       = obj.getString("answer_b"),
                            answerC       = obj.getString("answer_c"),
                            answerD       = obj.getString("answer_d"),
                            correctAnswer = obj.getString("correct_answer"),
                            timer         = timerValue
                        )
                    )
                }
                updateQuestionNumber()
                prefillOrClear(1)
            },
            { error -> Log.e(TAG, "Questions load error", error) }
        ))
    }

    private fun prefillOrClear(index: Int) {
        if (questionsList.size >= index) {
            val q = questionsList[index - 1]
            edtQuestion.setText(q.questionText)
            edtAnswerA.setText(q.answerA)
            edtAnswerB.setText(q.answerB)
            edtAnswerC.setText(q.answerC)
            edtAnswerD.setText(q.answerD)
            selectAnswer(q.correctAnswer)
        } else {
            edtQuestion.text.clear()
            edtAnswerA.text.clear()
            edtAnswerB.text.clear()
            edtAnswerC.text.clear()
            edtAnswerD.text.clear()
            correctAnswer = ""
            clearSelection()
        }
    }

    private fun updateQuestionNumber() {
        tvQuestionNumber.text = "Pertanyaan $currentQuestionIndex/10"
    }

    private fun validateInputs(): Boolean {
        if (edtQuestion.text.isBlank() ||
            edtAnswerA.text.isBlank()   ||
            edtAnswerB.text.isBlank()   ||
            edtAnswerC.text.isBlank()   ||
            edtAnswerD.text.isBlank()   ||
            correctAnswer.isEmpty()
        ) {
            Toast.makeText(this, "Please complete all fields and select the correct answer", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveCurrent() {
        val q = Question(
            questionText  = edtQuestion.text.toString(),
            answerA       = edtAnswerA.text.toString(),
            answerB       = edtAnswerB.text.toString(),
            answerC       = edtAnswerC.text.toString(),
            answerD       = edtAnswerD.text.toString(),
            correctAnswer = correctAnswer,
            timer         = timerValue
        )
        if (questionsList.size >= currentQuestionIndex) {
            questionsList[currentQuestionIndex - 1] = q
        } else {
            questionsList.add(q)
        }
    }

    private fun selectAnswer(ans: String) {
        clearSelection()
        correctAnswer = ans
        val btn = when (ans) {
            "A" -> answerAButton
            "B" -> answerBButton
            "C" -> answerCButton
            else -> answerDButton
        }
        btn.setBackgroundResource(R.drawable.check_image)
    }

    private fun clearSelection() {
        answerAButton.setBackgroundResource(R.drawable.answer_edit)
        answerBButton.setBackgroundResource(R.drawable.answer_edit)
        answerCButton.setBackgroundResource(R.drawable.answer_edit)
        answerDButton.setBackgroundResource(R.drawable.answer_edit)
    }
}