package com.example.indokuistampilan2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.indokuistampilan2.model.Question
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class edit_minigame2 : AppCompatActivity() {
    companion object {
        private const val MAX_QUESTIONS = 5
        private const val BASE_URL = "http://192.168.8.137/indokuis"
    }

    private var topicId = 0
    private var timerValue: String = ""
    private lateinit var questions: ArrayList<Question>
    private var correctOption = ""

    private lateinit var etQuestion: EditText
    private lateinit var etA: EditText
    private lateinit var etB: EditText
    private lateinit var etC: EditText
    private lateinit var etD: EditText
    private lateinit var btnSave: Button
    private lateinit var backButton: ImageButton
    private lateinit var answerAButton : ImageButton
    private lateinit var answerBButton : ImageButton
    private lateinit var answerCButton : ImageButton
    private lateinit var answerDButton : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_minigame2)

        topicId    = intent.getIntExtra("topic_id", 0)
        timerValue = intent.getStringExtra("timer") ?: ""
        questions  = intent.getParcelableArrayListExtra("questions") ?: arrayListOf()

        etQuestion    = findViewById(R.id.etQuestion)
        etA           = findViewById(R.id.etOptionA)
        etB           = findViewById(R.id.etOptionB)
        etC           = findViewById(R.id.etOptionC)
        etD           = findViewById(R.id.etOptionD)
        btnSave       = findViewById(R.id.btn_simpan)
        backButton    = findViewById(R.id.btn_back)
        answerAButton = findViewById(R.id.answerAButton)
        answerBButton = findViewById(R.id.answerBButton)
        answerCButton = findViewById(R.id.answerCButton)
        answerDButton = findViewById(R.id.answerDButton)

        // Prefill Q5 if saved
        if (questions.size >= MAX_QUESTIONS) {
            val q = questions[MAX_QUESTIONS - 1]
            etQuestion.setText(q.questionText)
            etA.setText(q.answerA)
            etB.setText(q.answerB)
            etC.setText(q.answerC)
            etD.setText(q.answerD)
            when (q.correctAnswer) {
                "A" -> answerAButton.setBackgroundResource(R.drawable.check_image)
                "B" -> answerBButton.setBackgroundResource(R.drawable.check_image)
                "C" -> answerCButton.setBackgroundResource(R.drawable.check_image)
                "D" -> answerDButton.setBackgroundResource(R.drawable.check_image)
            }
            correctOption = q.correctAnswer
        }

        answerAButton.setOnClickListener { selectAnswer("A") }
        answerBButton.setOnClickListener { selectAnswer("B") }
        answerCButton.setOnClickListener { selectAnswer("C") }
        answerDButton.setOnClickListener { selectAnswer("D") }

        btnSave.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            saveCurrent()
            submitAllQuestions()
        }
        backButton.setOnClickListener { finish() }
    }

    private fun validateInputs() = if (
        etQuestion.text.isBlank() ||
        etA.text.isBlank()         ||
        etB.text.isBlank()         ||
        etC.text.isBlank()         ||
        etD.text.isBlank()         ||
        correctOption.isEmpty()
    ) {
        Toast.makeText(this, "Fill all fields and choose correct option", Toast.LENGTH_SHORT).show()
        false
    } else true

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
        if (questions.size >= MAX_QUESTIONS) {
            questions[MAX_QUESTIONS - 1] = q
        } else {
            questions.add(q)
        }
    }

    private fun submitAllQuestions() {
        val payload = JSONObject().apply {
            put("topic_id", topicId)
            put("questions", JSONArray().apply {
                questions.forEach { q ->
                    put(JSONObject().apply {
                        put("question_text",  q.questionText)
                        put("option_a",       q.answerA)
                        put("option_b",       q.answerB)
                        put("option_c",       q.answerC)
                        put("option_d",       q.answerD)
                        put("correct_option", q.correctAnswer)
                        put("timer",          q.timer)
                    })
                }
            })
        }

        val url = "$BASE_URL/save_minigame_questions.php"
        JsonObjectRequest(Request.Method.POST, url, payload,
            { _ ->
                Toast.makeText(this, "Minigame saved!", Toast.LENGTH_SHORT).show()
                finish()
            },
            { error ->
                Toast.makeText(this, "Error saving: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        ).also { Volley.newRequestQueue(this).add(it) }
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
