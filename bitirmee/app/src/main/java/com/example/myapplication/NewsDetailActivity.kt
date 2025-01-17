package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class NewsDetailActivity : AppCompatActivity() {
    private lateinit var titleTextView: TextView
    private lateinit var contentWebView: WebView
    private lateinit var dateTimeTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var webViewButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        titleTextView = findViewById(R.id.newsDetailTitle)
        contentWebView = findViewById(R.id.newsDetailContent)
        dateTimeTextView = findViewById(R.id.newsDetailDateTime)
        progressBar = findViewById(R.id.detailProgressBar)
        webViewButton = findViewById(R.id.webViewButton)

        // WebView ayarları
        contentWebView.settings.apply {
            javaScriptEnabled = false
            defaultFontSize = 16
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }


        val title = intent.getStringExtra("title") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val link = intent.getStringExtra("link") ?: ""

        titleTextView.text = title
        dateTimeTextView.text = "$date - $time"

        fetchNewsDetail(link)

        webViewButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(browserIntent)
        }
    }

    private fun fetchNewsDetail(url: String) {
        progressBar.visibility = View.VISIBLE
        contentWebView.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NewsDetail", "URL'ye bağlanılıyor: $url")
                val doc = Jsoup.connect(url)
                    .timeout(10000)
                    .get()

                val contentDiv: Elements = doc.select("div#ContentPlaceHolder1_divNewsDetailText")

                // İstenmeyen linkleri kaldır
                contentDiv.select("a[href]").forEach { element ->
                    if (element.text().contains("diğer haberleri için tıklayınız")) {
                        element.remove()
                    }
                }

                // HTML stillerini ekle
                val htmlStyle = """
                    <style>
                        body { 
                            color: white; 
                            background-color: black;
                            font-family: Arial, sans-serif;
                            padding: 6px;
                            line-height: 1.4;
                            font-size: 14px;
                        }
                        table { 
                            width: 100%; 
                            border-collapse: collapse; 
                            margin: 6px 0;
                            background-color: #1a1a1a;
                           font-size: 12px;
                        }
                        td { 
                            padding: 8px 6px; 
                            border: 1px solid #444;
                            font-size: 12px;
                             vertical-align: top;
                        }
                        td.ilk {
                            background-color: #222;
                            font-weight: bold;
                           width: 40%;
                        }
                        a { 
                            color: #2196F3; 
                            text-decoration: none;
                        }
                         p { 
                            margin: 6px 0;
                           font-size: 12px;
                        }
                       img {
                           max-width: 100%;
                           height: auto;
                       }
                        .bas {
                            font-weight: bold;
                            color: #ccc;
                            margin-top: 12px;
                        }
                    </style>
                   <meta name="viewport" content="width=device-width, initial-scale=1.0">
                """.trimIndent()

                val htmlContent = contentDiv.html()
                val fullHtml = "<html><head>$htmlStyle</head><body>$htmlContent</body></html>"

                withContext(Dispatchers.Main) {
                    contentWebView.loadDataWithBaseURL(
                        null,
                        fullHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                    contentWebView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }

            } catch (e: Exception) {
                Log.e("NewsDetail", "Hata oluştu", e)
                withContext(Dispatchers.Main) {
                    val errorHtml = """
                        <html>
                        <head>
                            <style>
                                body { 
                                    color: white; 
                                    background-color: black;
                                    font-family: Arial, sans-serif;
                                    padding: 16px;
                                }
                            </style>
                        </head>
                        <body>
                            <p>Haber detayı yüklenirken bir hata oluştu: ${e.message}</p>
                        </body>
                        </html>
                    """.trimIndent()

                    contentWebView.loadData(
                        errorHtml,
                        "text/html",
                        "UTF-8"
                    )
                    contentWebView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
                e.printStackTrace()
            }
        }
    }
}