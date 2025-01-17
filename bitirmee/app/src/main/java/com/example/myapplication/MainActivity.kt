package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : BaseActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var hisseVerileriRef: DatabaseReference
    private lateinit var container: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var tvDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentLayout(R.layout.activity_main)

        database = FirebaseDatabase.getInstance("https://users-59f1f-default-rtdb.europe-west1.firebasedatabase.app")
        hisseVerileriRef = database.getReference("hisse_verileri")
        sharedPreferences = getSharedPreferences("HisseSecimleri", Context.MODE_PRIVATE)

        container = findViewById(R.id.container)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        tvDate = findViewById(R.id.tvDate)


        swipeRefreshLayout.setOnRefreshListener {
            loadSelectedHisseler()
        }
        updateDate()
        loadSelectedHisseler()
    }

    private fun updateDate() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())
        tvDate.text = currentDate
    }
    private fun loadSelectedHisseler() {
        container.removeAllViews()
        var hasAnyHisse = false
        val amerikanBorsaSecili = sharedPreferences.getBoolean("amerikan_borsa", false)
        val borsaIstanbulSecili = sharedPreferences.getBoolean("borsa_istanbul", false)


        if (amerikanBorsaSecili) {
            hasAnyHisse=true;
            loadHisseler("amerikan_borsa")
        }

        if (borsaIstanbulSecili) {
            hasAnyHisse = true;
            loadHisseler("borsa_istanbul")
        }

        if (!hasAnyHisse) {
            val textView = TextView(this)
            textView.text = "Henüz bir hisse kategorisi seçmediniz"
            textView.gravity = Gravity.CENTER
            textView.textSize=20f;
            textView.setTextColor(Color.BLACK)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // Genişliği match_parent yap
                LinearLayout.LayoutParams.WRAP_CONTENT // Yüksekliği wrap_content yap
            )
            layoutParams.setMargins(0,100,0,0)
            textView.layoutParams=layoutParams
            container.addView(textView)
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun loadHisseler(kategori:String){
        hisseVerileriRef.child(kategori).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (hisse in snapshot.children) {
                        val hisseAdi = hisse.key ?: continue
                        val kapanis = hisse.child("Kapanis").child("0").getValue(Double::class.java)?:0.0
                        val daily = hisse.child("Daily").child("0").getValue(Double::class.java)?:0.0
                        addTableRow(hisseAdi,kapanis.toString(),daily.toString())
                    }
                }else {
                    showErrorText("Veri çekme hatası.")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("FirebaseDebug", "Failed to read value.", error.toException())
                showErrorText("Veri çekme hatası.")
            }
        })
    }
    private fun addTableRow(symbol: String, price: String, change: String) {
        val cardView = CardView(this)
        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.setMargins(0, 8, 0, 8)
        cardView.layoutParams = cardParams
        cardView.radius = 8f
        cardView.cardElevation = 4f


        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(16, 16, 16, 16)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = params

        row.setBackgroundColor(getColor(R.color.darkkk_gray))

        val tvSymbol = TextView(this)
        tvSymbol.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvSymbol.text = symbol
        tvSymbol.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        tvSymbol.setTextColor(Color.WHITE)
        val tvPrice = TextView(this)
        tvPrice.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvPrice.text = price
        tvPrice.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        tvPrice.setTextColor(Color.WHITE)


        val tvChange = TextView(this)
        tvChange.layoutParams =
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tvChange.text = change
        tvChange.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        tvChange.setTextColor(
            if (change.startsWith("-")) Color.RED
            else Color.parseColor("#4CAF50")
        )
        row.addView(tvSymbol)
        row.addView(tvPrice)
        row.addView(tvChange)


        cardView.addView(row)

        cardView.setOnClickListener {
            val intent = Intent(this, CurrencyDetailActivity::class.java)
            intent.putExtra("symbol", symbol)
            intent.putExtra("price", price)
            intent.putExtra("change", change)
            startActivity(intent)
        }
        container.addView(cardView)

    }
    private fun showErrorText(errorMessage: String){
        val textView = TextView(this)
        textView.text = errorMessage
        textView.setTextColor(Color.RED)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, // Genişliği wrap_content yap
            LinearLayout.LayoutParams.WRAP_CONTENT // Yüksekliği wrap_content yap
        )
        layoutParams.setMargins(0,20,0,0)
        textView.layoutParams = layoutParams
        container.addView(textView)
    }
    override fun onResume() {
        super.onResume()
        loadSelectedHisseler()
    }

}