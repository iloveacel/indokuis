package com.example.indokuistampilan2

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException


class dashboard_siswa : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicAdapter: TopicAdapter
    private val topicList = ArrayList<Topic>()

    private lateinit var tvNama: TextView
    private lateinit var tvKelas: TextView
    private var backPressedTime: Long = 0
    private lateinit var backToast: Toast

    private var nama: String = ""
    private var username: String = ""
    private var kelas: String = ""

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            backToast.cancel()
            super.onBackPressed()
        } else {
            backToast = Toast.makeText(this, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT)
            backToast.show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_siswa)

        // Get data from intent
        nama = intent.getStringExtra("nama_siswa") ?: ""
        username = intent.getStringExtra("username") ?: ""
        kelas = intent.getStringExtra("kelas") ?: ""

        // Initialize TextViews
        tvNama = findViewById(R.id.txt_nama_siswa)
        tvKelas = findViewById(R.id.txt_kelas_siswa)

        // Set text
        tvNama.text = "Nama: $nama"
        tvKelas.text = "Kelas: $kelas"

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerViewTopics)
        recyclerView.layoutManager = LinearLayoutManager(this)
        topicAdapter = TopicAdapter(topicList)
        recyclerView.adapter = topicAdapter

        // Load topics from server
        loadTopicsForStudent()
    }

    private fun loadTopicsForStudent() {
        val url = "http://192.168.8.137/indokuis/get_topics.php"  // Use correct URL

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val topicsList = mutableListOf<Topic>()
                try {
                    for (i in 0 until response.length()) {
                        val topicObject = response.getJSONObject(i)
                        val topicId = topicObject.getInt("id")  // Parsing the id
                        val topicName = topicObject.getString("nama_topik")
                        val topicDescription = topicObject.getString("deskripsi")
                        val topicDeadline = topicObject.getString("tenggat_waktu") // Parse the deadline as well

                        val topic = Topic(topicId, topicName, topicDescription, topicDeadline)  // Include the ID here
                        topicsList.add(topic)
                    }
                    topicsList.reverse()

                    topicAdapter.updateTopics(topicsList)

                } catch (e: JSONException) {
                    Toast.makeText(this, "Error parsing JSON response", Toast.LENGTH_SHORT).show()
                    Log.e("JSON_ERROR", "Error: ${e.message}")
                }
            },
            { error ->
                Toast.makeText(this, "Volley error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("Volley_ERROR", "Error: ${error.message}")
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request)
    }
}
