package com.example.indokuistampilan2

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class login_page : AppCompatActivity() {

    lateinit var UsernameInput: EditText
    lateinit var PasswordInput: EditText
    lateinit var LoginButton: Button
    lateinit var DaftarButton: Button
    lateinit var RoleRadioGroup: RadioGroup
    lateinit var RadioSiswa: RadioButton
    lateinit var RadioGuru: RadioButton
    private var backPressedTime: Long = 0
    private lateinit var backToast: Toast

    override fun onBackPressed() {
        if (backPressedTime + 1500 > System.currentTimeMillis()) {
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)

        UsernameInput = findViewById(R.id.username)
        PasswordInput = findViewById(R.id.pass)
        LoginButton = findViewById(R.id.btn_login)
        DaftarButton = findViewById(R.id.btn_daftar)
        RoleRadioGroup = findViewById(R.id.roleRadioGroup)
        RadioSiswa = findViewById(R.id.radioSiswa)
        RadioGuru = findViewById(R.id.radioGuru)

        LoginButton.setOnClickListener {
            val username = UsernameInput.text.toString()
            val password = PasswordInput.text.toString()
            val selectedRole = when (RoleRadioGroup.checkedRadioButtonId) {
                R.id.radioSiswa -> "siswa"
                R.id.radioGuru -> "guru"
                else -> ""
            }

            if (username.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {
                Toast.makeText(this, "Isi semua data dan pilih peran!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_loading, null)
            val loadingDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()
            loadingDialog.show()

            val url = "http://192.168.8.137/indokuis/login.php"

            val stringRequest = object : StringRequest(Method.POST, url,
                { response ->
                    loadingDialog.dismiss()

                    val trimmedResponse = response.trim()
                    Log.d("RAW_RESPONSE", "Raw response: $trimmedResponse")

                    try {
                        val jsonResponse = JSONObject(trimmedResponse)
                        val status = jsonResponse.getString("status")

                        if (status == "success") {
                            val nama = jsonResponse.getString("nama")
                            val roleFromServer = jsonResponse.getString("role").trim().lowercase()

                            if (selectedRole != roleFromServer) {
                                Toast.makeText(this, "Peran tidak sesuai dengan akun yang dipilih!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Berhasil login sebagai $roleFromServer", Toast.LENGTH_SHORT).show()

                                val fadeOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out).toBundle()
                                } else null

                                when (roleFromServer) {
                                    "siswa" -> {
                                        val kelas = jsonResponse.getString("kelas")
                                        val intent = Intent(this, dashboard_siswa::class.java)
                                        intent.putExtra("nama_siswa", nama)
                                        intent.putExtra("kelas", kelas)
                                        if (fadeOptions != null) startActivity(intent, fadeOptions)
                                        else {
                                            startActivity(intent)
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                        }
                                        finish()
                                    }
                                    "guru" -> {
                                        val intent = Intent(this, dashboard_guru::class.java)
                                        intent.putExtra("nama_guru", nama)
                                        intent.putExtra("nip_guru", username)
                                        if (fadeOptions != null) startActivity(intent, fadeOptions)
                                        else {
                                            startActivity(intent)
                                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                                        }
                                        finish()
                                    }
                                    else -> {
                                        Toast.makeText(this, "Role tidak dikenali: $roleFromServer", Toast.LENGTH_SHORT).show()
                                        Log.d("ROLE_DEBUG", "Unrecognized role: '$roleFromServer'")
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, "Login gagal: kredensial salah", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Show raw response in dialog
                        AlertDialog.Builder(this)
                            .setTitle("Response Parsing Error")
                            .setMessage("Response:\n$trimmedResponse")
                            .setPositiveButton("OK", null)
                            .show()
                        Log.e("PARSE_ERROR", "Exception: ${e.message}")
                        e.printStackTrace()
                    }
                },
                { error ->
                    loadingDialog.dismiss()
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LOGIN_ERROR", "Volley error: ${error.message}")
                }) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username"] = username
                    params["password"] = password
                    return params
                }
            }

            Volley.newRequestQueue(this).add(stringRequest)
        }

        DaftarButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
