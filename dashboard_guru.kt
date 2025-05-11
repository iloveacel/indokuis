package com.example.indokuistampilan2

import android.content.Intent
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONException

class dashboard_guru : AppCompatActivity() {

    private lateinit var txtNamaGuru: TextView
    private lateinit var txtNipGuru: TextView
    private lateinit var btnAddTopic: FloatingActionButton
    private var backPressedTime: Long = 0
    private lateinit var backToast: Toast

    private lateinit var recyclerView: RecyclerView
    private lateinit var topicAdapter: TeacherTopicAdapter
    private val topicList = ArrayList<Topic>()

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
        setContentView(R.layout.activity_dashboard_guru)

        txtNamaGuru = findViewById(R.id.nama_guru)
        txtNipGuru = findViewById(R.id.nip_guru)
        btnAddTopic = findViewById(R.id.fabAddTopic)

        recyclerView = findViewById(R.id.recyclerViewTopics)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Set the Adapter with the click listener to navigate to the edit_kuis page
        topicAdapter = TeacherTopicAdapter(this, topicList) { topic ->
            // Log the topic ID to ensure it's being passed correctly
            Log.d("TopicClicked", "Topic ID: ${topic.id}, Name: ${topic.nama_topik}")

            // Check if the topic ID is valid
            if (topic.id != 0) {
                // When a topic is clicked, navigate to the edit_kuis page and pass the topic ID
                val intent = Intent(this, edit_kuis::class.java)
                intent.putExtra("topic_id", topic.id) // Pass the topic ID
                startActivity(intent)
            } else {
                Log.e("Error", "Topic ID is 0 or invalid")
            }
        }

        recyclerView.adapter = topicAdapter

        // Add New Topic Button Click Listener
        btnAddTopic.setOnClickListener {
            val topicId = 0 // For new topic, the ID will be 0
            val intent = Intent(this, edit_informasi_kuis::class.java)
            intent.putExtra("topic_id", topicId)
            startActivity(intent)
        }

        // Load topics when the activity is created
        loadTopics()
    }

    override fun onResume() {
        super.onResume()

        // Retrieve the teacher's name and NIP from SharedPreferences
        val sharedPreferences = getSharedPreferences("TeacherPrefs", MODE_PRIVATE)
        val namaGuru = sharedPreferences.getString("TEACHER_NAME", "No Name")
        val nipGuru = sharedPreferences.getString("TEACHER_NIP", "No NIP")

        // Log the retrieved data to verify
        Log.d("DashboardGuru", "Retrieved Nama Guru: $namaGuru, Retrieved NIP Guru: $nipGuru")

        // Set the teacher's name and NIP in the TextViews
        txtNamaGuru.text = "Nama Guru : $namaGuru"
        txtNipGuru.text = "NIP : $nipGuru"
    }

    private fun loadTopics() {
        val url = "http://192.168.8.137/indokuis/get_topics.php"

        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                topicList.clear() // Clear the list to prevent duplicates
                try {
                    if (response.length() == 0) {
                        Log.e("loadTopics", "No topics found")
                        Toast.makeText(this, "No topics found", Toast.LENGTH_SHORT).show()
                    } else {
                        // Parse the response
                        for (i in 0 until response.length()) {
                            val topicObject = response.getJSONObject(i)
                            val topic = Topic(
                                topicObject.getInt("id"),
                                topicObject.getString("nama_topik"),
                                topicObject.getString("deskripsi"),
                                topicObject.getString("tenggat_waktu") // Correctly map the tenggat_waktu field
                            )
                            topicList.add(topic)
                        }

                        topicList.reverse() // Show the most recent topics at the top
                        topicAdapter.notifyDataSetChanged() // Notify the adapter to update the UI
                    }
                } catch (e: JSONException) {
                    Log.e("JSON_ERROR", "Error parsing response: ${e.message}")
                    Toast.makeText(this, "Error loading topics", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("Volley_ERROR", "Volley error: ${error.localizedMessage}")
                Toast.makeText(this, "Error fetching topics", Toast.LENGTH_SHORT).show()
            }
        )

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request)
    }
}
