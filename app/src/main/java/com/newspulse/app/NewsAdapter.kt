package com.newspulse.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.newspulse.app.databinding.ItemNewsBinding

class NewsAdapter(
    private var articles: MutableList<NewsArticle>,
    private val onArticleClick: (NewsArticle) -> Unit,
    private val onBookmarkClick: (NewsArticle, Int) -> Unit,
    private val onShareClick: (NewsArticle) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        val binding = holder.binding
        val context = binding.root.context

        binding.tvChannelBadge.text = article.source
        try {
            binding.tvChannelBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor(article.getSourceColorHex()))
        } catch (_: Exception) {
            binding.tvChannelBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.badge_general))
        }

        binding.tvCategory.text = article.category
        binding.tvPubTime.text = article.getFormattedTime()
        binding.tvTitle.text = article.title
        binding.tvDescription.text = article.description

        if (article.isBookmarked) {
            binding.btnBookmark.setImageResource(R.drawable.ic_bookmark_filled)
            binding.btnBookmark.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_red))
        } else {
            binding.btnBookmark.setImageResource(R.drawable.ic_bookmark)
            binding.btnBookmark.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary))
        }

        binding.root.setOnClickListener {
            onArticleClick(article)
        }

        binding.btnReadStory.setOnClickListener {
            onArticleClick(article)
        }

        binding.btnBookmark.setOnClickListener {
            onBookmarkClick(article, position)
        }

        binding.btnShare.setOnClickListener {
            onShareClick(article)
        }
    }

    override fun getItemCount(): Int = articles.size

    fun updateData(newArticles: List<NewsArticle>) {
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }

    fun notifyBookmarkChanged(position: Int) {
        notifyItemChanged(position)
    }
}
