package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class TumHisselerActivity : BaseActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var hisseVerileriRef: DatabaseReference
    private lateinit var container: LinearLayout
    private lateinit var tvDate: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_tum_hisseler)
        // Firebase bağlantısı başlatılıyor ve root referansını alın
        database = FirebaseDatabase.getInstance("https://users-59f1f-default-rtdb.europe-west1.firebasedatabase.app") // **BURAYA KENDİ URL'NİZİ YAZIN**
        hisseVerileriRef = database.getReference("hisse_verileri")

        container = findViewById(R.id.container)
        tvDate = findViewById(R.id.tvDate)

        sharedPreferences = getSharedPreferences("HisseSecimleri", Context.MODE_PRIVATE)

        updateDate()
        readDataFromFirebase()
    }


    private fun updateDate() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        tvDate.text = currentDate
    }

    private fun readDataFromFirebase() {
        Log.d("FirebaseDebug", "readDataFromFirebase metodu çağrıldı.")

        hisseVerileriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FirebaseDebug", "onDataChange metodu çağrıldı.")

                container.removeAllViews() // Önceki verileri temizle

                if (snapshot.exists()) {
                    Log.d("FirebaseDebug", "Snapshot exists.")


                    val amerikanBorsaCheckBox = CheckBox(this@TumHisselerActivity)
                    amerikanBorsaCheckBox.text = "Amerikan Borsası"
                    amerikanBorsaCheckBox.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    amerikanBorsaCheckBox.setPadding(16,16,16,16)
                    amerikanBorsaCheckBox.textSize= 18f
                    amerikanBorsaCheckBox.setTextColor(Color.WHITE)

                    amerikanBorsaCheckBox.setOnCheckedChangeListener { _, isChecked ->
                        saveHisseSelection("amerikan_borsa", isChecked)
                    }

                    val savedStateAmerikan = sharedPreferences.getBoolean("amerikan_borsa", false)
                    amerikanBorsaCheckBox.isChecked = savedStateAmerikan;

                    container.addView(amerikanBorsaCheckBox)



                    val borsaIstanbulCheckBox = CheckBox(this@TumHisselerActivity)
                    borsaIstanbulCheckBox.text = "Borsa İstanbul"
                    borsaIstanbulCheckBox.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    borsaIstanbulCheckBox.setPadding(16,16,16,16)
                    borsaIstanbulCheckBox.textSize = 18f
                    borsaIstanbulCheckBox.setTextColor(Color.WHITE)

                    borsaIstanbulCheckBox.setOnCheckedChangeListener { _, isChecked ->
                        saveHisseSelection("borsa_istanbul", isChecked)
                    }
                    val savedStateBorsa = sharedPreferences.getBoolean("borsa_istanbul", false)
                    borsaIstanbulCheckBox.isChecked = savedStateBorsa;

                    container.addView(borsaIstanbulCheckBox)


                } else {
                    Log.w("FirebaseDebug", "Snapshot does not exist.") // Snapshot yoksa log'a yaz.
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseDebug", "Failed to read value. Hata: ${error.message}", error.toException())
            }
        })
    }

    private fun saveHisseSelection(hisseAdi: String, isChecked: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(hisseAdi, isChecked)
        editor.apply()
        Log.d("SharedPref", "$hisseAdi : $isChecked")
    }
}