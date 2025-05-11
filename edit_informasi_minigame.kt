// edit_informasi_minigame.kt
package com.example.indokuistampilan2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class edit_informasi_minigame : AppCompatActivity() {

    private lateinit var tvJudulTopik: TextView
    private lateinit var edtDurasi: EditText
    private lateinit var edtDeskripsi: EditText
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button

    private var topicId: Int = 0

    companion object {
        private const val TAG = "EditInfoMinigame"
        private const val BASE_URL = "http://192.168.8.137/indokuis"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_informasi_minigame)

        // 1) Bind views
        tvJudulTopik = findViewById(R.id.tvJudulTopik)
        edtDurasi    = findViewById(R.id.etDurasi)
        edtDeskripsi = findViewById(R.id.etDeskripsi)
        backButton   = findViewById(R.id.btnBackToDashboard)
        saveButton   = findViewById(R.id.btn_simpan)

        // 2) Read and validate topic_id
        topicId = intent.getIntExtra("topic_id", 0)
        if (topicId <= 0) {
            Toast.makeText(this, "Invalid Topic ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 3) Clear fields until loaded
        edtDurasi.setText("")
        edtDeskripsi.setText("")

        // 4) Load from server
        loadTopicDetails()

        // 5) Hook up buttons
        backButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { insertMinigameInfo() }

        // 6) Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    private fun loadTopicDetails() {
        val url = "$BASE_URL/get_minigame_topics.php?id=$topicId"
        Log.d(TAG, "Requesting URL: $url")

        val queue = Volley.newRequestQueue(this)
        val jsonReq = JsonObjectRequest(Request.Method.GET, url, null,
            { resp ->
                Log.d(TAG, "Response: $resp")
                // 1) Check status
                if (resp.optString("status") != "success") {
                    val msg = resp.optString("message", "Failed to load")
                    Log.w(TAG, "Server error: $msg")
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    return@JsonObjectRequest
                }
                try {
                    // 2) Grab the right object
                    val t: JSONObject = when {
                        resp.has("topic")    -> resp.getJSONObject("topic")
                        resp.has("minigame") -> resp.getJSONObject("minigame")
                        else -> {
                            Log.e(TAG, "No 'topic' or 'minigame' key in JSON")
                            Toast.makeText(this, "Unexpected server response", Toast.LENGTH_SHORT).show()
                            return@JsonObjectRequest
                        }
                    }
                    Log.d(TAG, "Parsed topic JSON: $t")

                    // 3) Populate UI
                    tvJudulTopik.text = t.optString("nama_topik", "")

                    val minDur = t.optInt("durasi_minigame", 0)
                    if (minDur > 0) {
                        edtDurasi.setText(minDur.toString())
                    }

                    val rawDesc = t.optString("deskripsi_minigame", "")
                    if (rawDesc.isNotBlank() && rawDesc.lowercase() != "null") {
                        edtDeskripsi.setText(rawDesc)
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "JSON parse error", e)
                    Toast.makeText(this, "Error parsing topic data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e(TAG, "Volley network error", error)
                Toast.makeText(this, "Network error while loading", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(jsonReq)
    }

    private fun insertMinigameInfo() {
        val dur  = edtDurasi.text.toString().trim()
        val desc = edtDeskripsi.text.toString().trim()

        if (dur.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Durasi & Deskripsi wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "$BASE_URL/update_minigame_info.php"
        Log.d(TAG, "Saving to URL: $url — durasi_minigame=$dur, deskripsi_minigame=$desc")

        val params = hashMapOf(
            "topic_id"           to topicId.toString(),
            "durasi_minigame"    to dur,
            "deskripsi_minigame" to desc
        )

        val saveReq = object : StringRequest(Method.POST, url,
            { raw ->
                val cleaned = raw.removePrefix("\uFEFF").trim()
                Log.d(TAG, "Save response raw: $cleaned")
                try {
                    val json   = JSONObject(cleaned)
                    val status = json.optString("status")
                    val msg    = json.optString("message")
                    Toast.makeText(
                        this,
                        if (status == "success") msg else "Error: $msg",
                        Toast.LENGTH_SHORT
                    ).show()

                    if (status == "success") {
                        Intent(this, edit_minigame::class.java).also {
                            it.putExtra("topic_id", topicId)
                            it.putExtra("timer", dur)
                            startActivity(it)
                        }
                        finish()
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Parse error on save", e)
                    Toast.makeText(this, "Invalid server response", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e(TAG, "Volley error on save", error)
                Toast.makeText(this, "Save failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> = params
        }

        Volley.newRequestQueue(this).add(saveReq)
    }
}
