package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class KayitActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kayit)

        // UI Bileşenlerini Bağla
        etName = findViewById(R.id.et_name)
        etSurname = findViewById(R.id.et_surname)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnRegister = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val surname = etSurname.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = Firebase.auth.currentUser?.uid
                        if (userId != null) {
                            val firestore = Firebase.firestore
                            val userMap = hashMapOf(
                                "name" to name,
                                "surname" to surname,
                                "email" to email
                            )

                            // ProfilActivity'ye hemen yönlendir
                            val intent = Intent(this, ProfilActivity::class.java)
                            intent.putExtra("USER_NAME", name)
                            startActivity(intent)
                            finish() // Bu aktiviteyi kapat

                            // Firestore'a veri kaydetmeye devam et
                            firestore.collection("users").document(userId).set(userMap)
                                .addOnCompleteListener { dbTask ->
                                    if (!dbTask.isSuccessful) {
                                        Log.e("KayitActivity", "Firestore'a yazma hatası: ${dbTask.exception}")
                                        Toast.makeText(this, "Bilgiler kaydedildi", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Kullanıcı bilgileri alınamadı.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorMessage = task.exception?.message ?: "Bilinmeyen bir hata oluştu"
                        Toast.makeText(this, "Kayıt başarısız: $errorMessage", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }
}
