package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import android.widget.ProgressBar
import android.widget.TextView
import java.io.IOException

class TakipListesiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorMessage: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var newsAdapter: NewsAdapter
    private var allNews: List<NewsItem> = emptyList()
    private var loading = false
    private val pageSize = 10
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_takip_listesi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        errorMessage = findViewById(R.id.errorMessage)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        newsAdapter = NewsAdapter(mutableListOf()) { newsItem ->
            val intent = Intent(this, NewsDetailActivity::class.java)
            intent.putExtra("title", newsItem.konu)
            intent.putExtra("date", newsItem.tarih)
            intent.putExtra("time", newsItem.saat)
            intent.putExtra("link", newsItem.link)
            startActivity(intent)
        }
        recyclerView.adapter = newsAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!loading && layoutManager.findLastVisibleItemPosition() >= newsAdapter.itemCount - 1) {
                    loadMoreItems()
                }
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            currentPage = 0
            newsAdapter.updateData(emptyList())
            fetchNewsData()
        }

        fetchNewsData()
    }

    private fun fetchNewsData() {
        currentPage = 0
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        errorMessage.visibility = View.GONE
        loading = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                allNews = fetchNews()
                withContext(Dispatchers.Main) {
                    loadPage(currentPage)
                    recyclerView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    swipeRefreshLayout.isRefreshing = false
                    loading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    errorMessage.visibility = View.VISIBLE
                    swipeRefreshLayout.isRefreshing = false
                    loading = false
                }
                e.printStackTrace()
            }
        }
    }

    private fun loadPage(page: Int) {
        loading = true
        val start = page * pageSize
        val end = minOf(start + pageSize, allNews.size)
        val pageData = if (start < allNews.size) {
            allNews.subList(start, end)
        } else {
            emptyList()
        }
        newsAdapter.addItems(pageData)
        currentPage++
        loading = false
    }

    private fun loadMoreItems() {
        if (allNews.isNotEmpty()) {
            loadPage(currentPage)
        }
    }

    suspend fun fetchNews(): List<NewsItem> {
        return withContext(Dispatchers.IO) {
            val url = "https://www.matriksdata.com/website/matriks-haberler"
            val newsList = mutableListOf<NewsItem>()
            try {
                val doc: Document = Jsoup.connect(url).get()
                val table: Elements = doc.select("table.table.table-striped tbody tr")

                table.forEach { row ->
                    val tarih = row.select("td:nth-child(1)").text()
                    val saat = row.select("td:nth-child(2)").text()
                    val konu = row.select("td:nth-child(3)").text().trim()
                    val link = row.attr("onclick")?.let { onclick ->
                        val matchResult = Regex("(?<=document\\.location=')([^']*)'").find(onclick)
                        matchResult?.value ?: ""
                    } ?: ""

                    newsList.add(NewsItem(tarih, saat, konu, "", link))
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    errorMessage.visibility = View.VISIBLE
                }
            }
            newsList
        }
    }
}