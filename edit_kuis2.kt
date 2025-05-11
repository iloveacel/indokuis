package com.example.indokuistampilan2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.indokuistampilan2.model.Question
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class edit_kuis2 : AppCompatActivity() {

    private var topicId = 0
    private lateinit var timerValue: String
    private lateinit var questions: ArrayList<Question>
    private var correctAnswer = ""

    private lateinit var edtQuestion: EditText
    private lateinit var edtA: EditText
    private lateinit var edtB: EditText
    private lateinit var edtC: EditText
    private lateinit var edtD: EditText
    private lateinit var tvTimer: TextView
    private lateinit var answerAButton: ImageButton
    private lateinit var answerBButton: ImageButton
    private lateinit var answerCButton: ImageButton
    private lateinit var answerDButton: ImageButton
    private lateinit var btnSave: Button
    private lateinit var btnBuatMinigam: Button
    private lateinit var backButton: ImageButton

    companion object {
        private const val TAG = "EditKuis2"
        private const val BASE_URL = "http://192.168.8.137/indokuis"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_kuis2)

        // Pull extras
        topicId    = intent.getIntExtra("topic_id", 0)
        timerValue = intent.getStringExtra("timer") ?: "0"
        questions  = intent.getParcelableArrayListExtra("questions_list") ?: arrayListOf()

        // Bind views
        edtQuestion    = findViewById(R.id.questionBox)
        edtA           = findViewById(R.id.answerA)
        edtB           = findViewById(R.id.answerB)
        edtC           = findViewById(R.id.answerC)
        edtD           = findViewById(R.id.answerD)
        tvTimer        = findViewById(R.id.timer)
        answerAButton  = findViewById(R.id.answerAButton)
        answerBButton  = findViewById(R.id.answerBButton)
        answerCButton  = findViewById(R.id.answerCButton)
        answerDButton  = findViewById(R.id.answerDButton)
        btnSave        = findViewById(R.id.btn_simpan)
        btnBuatMinigam = findViewById(R.id.btn_minigame)
        backButton     = findViewById(R.id.btn_back)

        // Show timer
        Log.d(TAG, "Received timer: $timerValue")
        tvTimer.text = timerValue

        // Reset selection
        correctAnswer = ""
        clearSelection()

        // Prefill Q10 if exists
        if (questions.size >= 10) {
            val last = questions[9]
            edtQuestion.setText(last.questionText)
            edtA.setText(last.answerA)
            edtB.setText(last.answerB)
            edtC.setText(last.answerC)
            edtD.setText(last.answerD)
            selectAnswer(last.correctAnswer)
        }

        // Selection callbacks
        answerAButton.setOnClickListener { selectAnswer("A") }
        answerBButton.setOnClickListener { selectAnswer("B") }
        answerCButton.setOnClickListener { selectAnswer("C") }
        answerDButton.setOnClickListener { selectAnswer("D") }

        // Back
        backButton.setOnClickListener { finish() }

        // Save
        btnSave.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            saveCurrent()
            submitAllQuestions {
                Toast.makeText(this, "Quiz saved!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Create minigame
        btnBuatMinigam.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            saveCurrent()
            submitAllQuestions {
                Toast.makeText(this, "Quiz saved!", Toast.LENGTH_SHORT).show()
                startActivity(
                    Intent(this, edit_informasi_minigame::class.java)
                        .putExtra("topic_id", topicId)
                )
                finish()
            }
        }
    }

    private fun selectAnswer(ans: String) {
        clearSelection()
        correctAnswer = ans
        val btn = when(ans) {
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

    private fun validateInputs(): Boolean {
        if (edtQuestion.text.isBlank() ||
            edtA.text.isBlank()        ||
            edtB.text.isBlank()        ||
            edtC.text.isBlank()        ||
            edtD.text.isBlank()        ||
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
            answerA       = edtA.text.toString(),
            answerB       = edtB.text.toString(),
            answerC       = edtC.text.toString(),
            answerD       = edtD.text.toString(),
            correctAnswer = correctAnswer,
            timer         = timerValue
        )
        if (questions.size >= 10) {
            questions[9] = q
        } else {
            questions.add(q)
        }
    }

    private fun submitAllQuestions(onSuccess: () -> Unit) {
        val payload = JSONObject().apply {
            put("topic_id", topicId)
            put("questions", JSONArray().apply {
                questions.forEach { q ->
                    put(JSONObject().apply {
                        put("question_text",  q.questionText)
                        put("answer_a",       q.answerA)
                        put("answer_b",       q.answerB)
                        put("answer_c",       q.answerC)
                        put("answer_d",       q.answerD)
                        put("correct_answer", q.correctAnswer)
                        put("timer",          q.timer)
                    })
                }
            })
        }
        val url = "$BASE_URL/save_question.php"
        JsonObjectRequest(Request.Method.POST, url, payload,
            { _ -> onSuccess() },
            { e -> Toast.makeText(this, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
        ).also { Volley.newRequestQueue(this).add(it) }
    }
}
