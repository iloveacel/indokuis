package com.example.indokuistampilan2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : AppCompatActivity() {

    lateinit var namaInput: EditText
    lateinit var usernameInput: EditText
    lateinit var passwordInput: EditText
    lateinit var konfirmasiPasswordInput: EditText
    lateinit var daftarButton: Button
    lateinit var loginButton: Button
    lateinit var a_button: Button
    lateinit var b_button: Button
    lateinit var c_button: Button
    lateinit var d_button: Button

    private var selectedClass: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        namaInput = findViewById(R.id.name)
        usernameInput = findViewById(R.id.username)
        passwordInput = findViewById(R.id.pass)
        konfirmasiPasswordInput = findViewById(R.id.konf_pass)
        daftarButton = findViewById(R.id.btn_daftar)
        loginButton = findViewById(R.id.btn_login)
        a_button = findViewById(R.id.btn_a)
        b_button = findViewById(R.id.btn_b)
        c_button = findViewById(R.id.btn_c)
        d_button = findViewById(R.id.btn_d)

        val classButtons = listOf(a_button, b_button, c_button, d_button)

        fun selectClass(button: Button, value: String) {
            selectedClass = value
            classButtons.forEach {
                it.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Button_Yellow))
            }
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.Header_Yellow))
        }

        a_button.setOnClickListener { selectClass(a_button, "A") }
        b_button.setOnClickListener { selectClass(b_button, "B") }
        c_button.setOnClickListener { selectClass(c_button, "C") }
        d_button.setOnClickListener { selectClass(d_button, "D") }

        daftarButton.setOnClickListener {
            val nama = namaInput.text.toString()
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val konfirmasiPassword = konfirmasiPasswordInput.text.toString()
            val kelas = selectedClass

            if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || konfirmasiPassword.isEmpty() || kelas == null) {
                Toast.makeText(this, "Semua data harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != konfirmasiPassword) {
                Toast.makeText(this, "Password dan Konfirmasi Password tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val url = "http://192.168.8.137/indokuis/register.php" // Make sure the IP is correct

            val stringRequest = object : StringRequest(Request.Method.POST, url,
                { response ->
                    val cleanedResponse = response.trim().lowercase()

                    when (cleanedResponse) {
                        "success" -> {
                            Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, login_page::class.java))
                            finish()
                        }
                        "duplicate" -> {
                            Toast.makeText(this, "Username sudah digunakan!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Terjadi kesalahan: $cleanedResponse", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["nama"] = nama
                    params["username"] = username
                    params["password"] = password
                    params["kelas"] = kelas
                    params["role"] = "siswa"
                    return params
                }
            }

            Volley.newRequestQueue(this).add(stringRequest)
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, login_page::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
