package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.reflect.KClass

open class BaseActivity : AppCompatActivity() {
    private lateinit var btnTumHisseler: Button
    private lateinit var btnProfil: Button
    private lateinit var btnTakipListesi: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)){
                v,insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left,systemBars.top,systemBars.right,systemBars.bottom)
            insets
        }
        btnTumHisseler = findViewById(R.id.btnTumHisseler)
        btnProfil = findViewById(R.id.btnProfil)
        btnTakipListesi = findViewById(R.id.btnTakipListesi)

        btnTumHisseler.setOnClickListener {
            navigateTo(TumHisselerActivity::class)

        }

        btnProfil.setOnClickListener {
            navigateTo(ProfilActivity::class)
        }

        btnTakipListesi.setOnClickListener {
            navigateTo(TakipListesiActivity::class)
        }
    }

    fun setContentLayout(layoutId: Int) {
        val contentFrame: FrameLayout = findViewById(R.id.content_frame)
        layoutInflater.inflate(layoutId, contentFrame, true)
    }

    private fun <T : AppCompatActivity> navigateTo(targetActivity: KClass<T>) {
        if(this::class != targetActivity )
        {
            startActivity(Intent(this, targetActivity.java))
        }
    }
}