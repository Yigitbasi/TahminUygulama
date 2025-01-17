package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NewsAdapter(
    private var newsList: MutableList<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsTitle: TextView = itemView.findViewById(R.id.newsTitle)
        val newsTime: TextView = itemView.findViewById(R.id.newsTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
        return NewsViewHolder(view)
    }

    override fun getItemCount() = newsList.size

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.newsTitle.text = newsItem.konu
        holder.newsTime.text = "${newsItem.tarih} - ${newsItem.saat}"

        holder.itemView.setOnClickListener {
            onItemClick(newsItem)
        }
    }


    fun addItems(newItems: List<NewsItem>) {
        val currentSize = newsList.size
        newsList.addAll(newItems)
        notifyItemRangeInserted(currentSize, newItems.size)
    }
    fun updateData(newNewsList: List<NewsItem>) {
        newsList.clear()
        newsList.addAll(newNewsList)
        notifyDataSetChanged()
    }
}