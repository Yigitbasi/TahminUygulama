package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class CurrencyDetailActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var hisseVerileriRef: DatabaseReference
    private lateinit var tvDetail: TextView
    private lateinit var tvSymbolInfo: TextView
    private lateinit var tvSymbolHeader: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val tahminGosterilecekIsimler = mapOf(
        "Seven_LM" to "7 Günlük (LM)",
        "Seven_RF" to "7 Günlük (RF)",
        "Three_LM" to "3 Günlük (LM)",
        "Three_RF" to "3 Günlük (RF)",
        "Tomorrow_LM" to "Yarın (LM)",
        "Tomorrow_RF" to "Yarın (RF)"
    )
    private var symbol: String = "TSLA" // Default symbol
    private var price: String? = null
    private var change: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_detail)

        database = FirebaseDatabase.getInstance("https://users-59f1f-default-rtdb.europe-west1.firebasedatabase.app")
        hisseVerileriRef = database.getReference("hisse_verileri")
        tvDetail = findViewById(R.id.tvCurrencyDetails)
        tvSymbolInfo = findViewById(R.id.tvSymbolInfo)
        tvSymbolHeader = findViewById(R.id.tvSymbolHeader)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)



        symbol = intent.getStringExtra("symbol") ?: "TSLA"
        price = intent.getStringExtra("price")
        change = intent.getStringExtra("change")

        fetchSymbolAndTahminler(symbol, price, change)

        swipeRefreshLayout.setOnRefreshListener {
            fetchSymbolAndTahminler(symbol, price, change)
        }

    }

    private fun fetchSymbolAndTahminler(symbol: String, price: String?, change: String?) {
        swipeRefreshLayout.isRefreshing = true
        var path = "amerikan_borsa"

        fetchSymbolName(symbol, price, change, path) { symbolName ->
            if(symbolName != null){
                fetchTahminler(symbol, path, price)
            } else {
                path = "borsa_istanbul"
                fetchSymbolName(symbol, price, change, path) {symbolName ->
                    if(symbolName != null){
                        fetchTahminler(symbol, path, price)
                    } else{
                        tvDetail.text = "Sembol veya tahminler bulunamadı."
                        swipeRefreshLayout.isRefreshing = false
                    }
                }

            }
        }
    }

    private fun fetchSymbolName(symbol: String, price: String?, change: String?, path: String, callback: (String?) -> Unit) {
        hisseVerileriRef.child(path).child(symbol)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val symbolName = snapshot.child("Sembol_Ismi").child("0").getValue(String::class.java)
                        if (symbolName != null) {
                            val symbolInfoBuilder = SpannableStringBuilder()
                            symbolInfoBuilder.append("Sembol: $symbol ($symbolName)\nFiyat: $price\nDeğişim: ")

                            if (change != null) {
                                val start = symbolInfoBuilder.length
                                symbolInfoBuilder.append(change)
                                val end = symbolInfoBuilder.length
                                if (change.startsWith("-")) {
                                    symbolInfoBuilder.setSpan(
                                        ForegroundColorSpan(Color.RED),
                                        start,
                                        end,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                } else {
                                    symbolInfoBuilder.setSpan(
                                        ForegroundColorSpan(Color.parseColor("#4CAF50")),
                                        start,
                                        end,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            } else {
                                symbolInfoBuilder.append("bilgi yok")
                            }

                            tvSymbolInfo.text = symbolInfoBuilder
                            tvSymbolHeader.text = symbol
                            callback(symbolName)
                        } else {
                            callback(null)
                        }
                    }else{
                        callback(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("FirebaseDebug", "Failed to read symbol name.", error.toException())
                    callback(null)
                    swipeRefreshLayout.isRefreshing = false
                }
            })
    }

    private fun fetchTahminler(symbol: String, path: String, price: String?) {
        hisseVerileriRef.child(path).child(symbol).child("Tahminler")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tahminlerBuilder = SpannableStringBuilder()
                    if (snapshot.exists()) {
                        val priceValue = price?.toDoubleOrNull() ?: 0.0
                        for (tahminSnapshot in snapshot.children) {
                            val tahminKey = tahminSnapshot.key
                            val tahminValue = tahminSnapshot.child("0").getValue(Double::class.java)

                            if (tahminKey != null && tahminValue != null) {
                                val gosterilecekIsim = tahminGosterilecekIsimler[tahminKey] ?: tahminKey
                                val percentageChange = if (priceValue != 0.0) {
                                    ((tahminValue - priceValue) / priceValue) * 100
                                } else {
                                    0.0
                                }

                                val formattedPercentage = String.format("%.2f", percentageChange)
                                val tahminMetni = "• $gosterilecekIsim: ${String.format("%.2f", tahminValue)} ("
                                val start = tahminlerBuilder.length
                                tahminlerBuilder.append(tahminMetni)


                                val percentageStart = tahminlerBuilder.length
                                tahminlerBuilder.append(formattedPercentage+"%")
                                val percentageEnd = tahminlerBuilder.length

                                if (formattedPercentage.startsWith("-")){
                                    tahminlerBuilder.setSpan(
                                        ForegroundColorSpan(Color.RED),
                                        percentageStart,
                                        percentageEnd,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }else{
                                    tahminlerBuilder.setSpan(
                                        ForegroundColorSpan(Color.parseColor("#4CAF50")),
                                        percentageStart,
                                        percentageEnd,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                                tahminlerBuilder.append(")\n")

                            } else {
                                Log.w("FirebaseDebug", "Value or key not found for $tahminKey")
                            }
                        }
                        tvDetail.text = tahminlerBuilder
                    }else{
                        tvDetail.text = "Tahminler bulunamadı."
                    }
                    swipeRefreshLayout.isRefreshing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("FirebaseDebug", "Failed to read tahminler.", error.toException())
                    tvDetail.text = "Tahminler alınırken hata oluştu."
                    swipeRefreshLayout.isRefreshing = false

                }
            })
    }}