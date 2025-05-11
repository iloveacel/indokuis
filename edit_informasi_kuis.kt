package com.example.indokuistampilan2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class edit_informasi_kuis : AppCompatActivity() {

    private lateinit var edtNamaTopik: EditText
    private lateinit var edtDeskripsi: EditText
    private lateinit var edtDurasi: EditText
    private lateinit var edtTenggatWaktu: EditText
    private lateinit var backButton: ImageButton
    private lateinit var saveButton: Button
    private var topicId: Int = 0 // Topic ID is set to 0 for new topics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_informasi_kuis)

        // Initialize views
        edtNamaTopik = findViewById(R.id.etJudulTopik)
        edtDeskripsi = findViewById(R.id.etDeskripsi)
        edtDurasi = findViewById(R.id.etDurasi)
        edtTenggatWaktu = findViewById(R.id.etTenggat)
        backButton = findViewById(R.id.btnBackToDashboard)
        saveButton = findViewById(R.id.btnSaveTopic)

        // Set up the Date and Time picker for the deadline (tenggat_waktu)
        edtTenggatWaktu.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Date Picker Dialog
            val datePickerDialog = DatePickerDialog(
                this, { _, selectedYear, selectedMonth, selectedDay ->
                    // Time Picker Dialog
                    val timePickerDialog = TimePickerDialog(
                        this, { _, selectedHour, selectedMinute ->
                            // Format the selected date and time
                            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                            val selectedTime = "$selectedHour:$selectedMinute"

                            // Combine date and time
                            val combinedDateTime = "$selectedDate $selectedTime"
                            edtTenggatWaktu.setText(combinedDateTime)  // Set the selected date and time into the EditText
                        },
                        hour, minute, true
                    )
                    timePickerDialog.show()
                },
                year, month, dayOfMonth
            )
            datePickerDialog.show()
        }

        // Always treat it as adding a new topic
        topicId = 0 // Set the topic_id to 0 to indicate adding a new topic

        // Set up the back button to navigate back to the dashboard
        backButton.setOnClickListener {
            val intent = Intent(this, dashboard_guru::class.java)
            startActivity(intent)
            finish()
        }

        // Set up the save button to insert the new topic when clicked
        saveButton.setOnClickListener {
            insertTopic()  // This function will insert a new topic into the database
        }

        // Set up the window insets (for edge-to-edge screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Function to insert the new topic into the database
    private fun insertTopic() {
        val url = "http://192.168.8.137/indokuis/insert_topics.php" // This is your URL

        // Prepare the data to send in the request
        val params = HashMap<String, String>()
        params["nama_topik"] = edtNamaTopik.text.toString()
        params["deskripsi"] = edtDeskripsi.text.toString()
        params["durasi_per_soal"] = edtDurasi.text.toString()

        // Format the deadline (tenggat_waktu) as YYYY-MM-DD HH:MM for insertion into the database
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        try {
            val date = dateFormat.parse(edtTenggatWaktu.text.toString())
            val formattedDate =
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
            params["tenggat_waktu"] = formattedDate
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            Log.e("DateError", "Error parsing date: ${e.message}")
            return
        }

        // Log the parameters being sent
        Log.d("VolleyRequest", "Sending params: $params")

        val request = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    Log.d("VolleyResponse", "Response: $response")
                    val jsonResponse = JSONObject(response)

                    // Check the response for success
                    if (jsonResponse.getString("status") == "success") {
                        Toast.makeText(this, "Topik Baru Berhasil Ditambahkan!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Failed to add new topic", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    // If the JSON response is invalid, log the error
                    Log.e("JSON_ERROR", "Error parsing response: ${e.message}")
                    Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                // Handle the Volley error
                Log.e("VolleyError", "Volley error: ${error.localizedMessage}")
                if (error.networkResponse != null) {
                    Log.e("VolleyError", "Error status code: ${error.networkResponse.statusCode}")
                    Log.e("VolleyError", "Error body: ${String(error.networkResponse.data)}")
                }
                Toast.makeText(this, "Volley error: ${error.localizedMessage}", Toast.LENGTH_LONG)
                    .show()
            }) {

            override fun getParams(): Map<String, String> {
                return params
            }
        }

        // Add the request to the Volley request queue
        Volley.newRequestQueue(this).add(request)
    }
}
