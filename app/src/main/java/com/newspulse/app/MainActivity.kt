package com.newspulse.app

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.newspulse.app.databinding.ActivityMainBinding
import com.newspulse.app.databinding.DialogArticleDetailBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: NewsDatabaseHelper
    private lateinit var adapter: NewsAdapter

    private var currentChannelFilter: String? = null
    private var isBookmarkedOnly: Boolean = false
    private var currentSearchQuery: String = ""

    private lateinit var chipViews: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = NewsDatabaseHelper(this)

        setupRecyclerView()
        setupChannelChips()
        setupSearch()
        setupActions()

        loadArticles()
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(
            articles = mutableListOf(),
            onArticleClick = { article -> showArticleDetailDialog(article) },
            onBookmarkClick = { article, position -> toggleBookmark(article, position) },
            onShareClick = { article -> shareArticle(article) }
        )

        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.primary_red, R.color.accent_blue)
        binding.swipeRefresh.setOnRefreshListener {
            refreshFeeds()
        }
    }

    private fun setupChannelChips() {
        chipViews = listOf(
            binding.chipAll,
            binding.chipToi,
            binding.chipHt,
            binding.chipNdtv,
            binding.chipHindu,
            binding.chipBbc,
            binding.chipSaved
        )

        binding.chipAll.setOnClickListener {
            selectChip(binding.chipAll, null, false)
        }
        binding.chipToi.setOnClickListener {
            selectChip(binding.chipToi, "Times of India", false)
        }
        binding.chipHt.setOnClickListener {
            selectChip(binding.chipHt, "Hindustan Times", false)
        }
        binding.chipNdtv.setOnClickListener {
            selectChip(binding.chipNdtv, "NDTV News", false)
        }
        binding.chipHindu.setOnClickListener {
            selectChip(binding.chipHindu, "The Hindu", false)
        }
        binding.chipBbc.setOnClickListener {
            selectChip(binding.chipBbc, "BBC News", false)
        }
        binding.chipSaved.setOnClickListener {
            selectChip(binding.chipSaved, null, true)
        }
    }

    private fun selectChip(selected: TextView, channel: String?, bookmarked: Boolean) {
        for (chip in chipViews) {
            chip.setBackgroundResource(R.drawable.bg_chip)
            chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        selected.setBackgroundResource(R.drawable.bg_chip_active)
        selected.setTextColor(ContextCompat.getColor(this, R.color.white))

        currentChannelFilter = channel
        isBookmarkedOnly = bookmarked
        loadArticles()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                binding.btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                loadArticles()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
            currentSearchQuery = ""
            binding.btnClearSearch.visibility = View.GONE
            loadArticles()
        }
    }

    private fun setupActions() {
        binding.btnRefresh.setOnClickListener {
            refreshFeeds()
        }

        binding.btnShareDigest.setOnClickListener {
            shareDailyDigest()
        }
    }

    private fun loadArticles() {
        val list = dbHelper.getArticles(
            sourceFilter = currentChannelFilter,
            bookmarkedOnly = isBookmarkedOnly,
            searchQuery = currentSearchQuery.ifEmpty { null }
        )

        adapter.updateData(list)

        if (list.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvNews.visibility = View.GONE
            binding.tvEmptyMessage.text = if (isBookmarkedOnly) {
                "No saved articles yet"
            } else if (currentSearchQuery.isNotEmpty()) {
                "No results for '$currentSearchQuery'"
            } else {
                "No stories available"
            }
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.rvNews.visibility = View.VISIBLE
        }

        binding.tvFeedStatus.text = when {
            isBookmarkedOnly -> "Showing ${list.size} saved bookmarks"
            currentChannelFilter != null -> "Showing ${list.size} stories from $currentChannelFilter"
            else -> "Showing ${list.size} stories • TOI, HT, NDTV, Hindu, BBC"
        }
    }

    private fun refreshFeeds() {
        binding.swipeRefresh.isRefreshing = true
        NewsFeedService.fetchAllFeeds { articles, success ->
            binding.swipeRefresh.isRefreshing = false
            if (success && articles.isNotEmpty()) {
                val inserted = dbHelper.insertArticles(articles)
                loadArticles()
                Toast.makeText(this, "Updated feeds with ${articles.size} live stories", Toast.LENGTH_SHORT).show()
            } else {
                loadArticles()
                Toast.makeText(this, "Refreshed: viewing latest news cache", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleBookmark(article: NewsArticle, position: Int) {
        val newStatus = !article.isBookmarked
        val success = dbHelper.toggleBookmark(article.id, newStatus)
        if (success) {
            article.isBookmarked = newStatus
            adapter.notifyBookmarkChanged(position)
            val msg = if (newStatus) "Saved to bookmarks" else "Removed from bookmarks"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

            if (isBookmarkedOnly) {
                loadArticles()
            }
        }
    }

    private fun shareArticle(article: NewsArticle) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Breaking: ${article.title}")
            putExtra(Intent.EXTRA_TEXT, article.getShareableText())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share News Story via")
        startActivity(shareIntent)
    }

    private fun shareDailyDigest() {
        val topArticles = dbHelper.getTopHeadlinesForDigest(5)
        if (topArticles.isEmpty()) {
            Toast.makeText(this, "No headlines available to share", Toast.LENGTH_SHORT).show()
            return
        }

        val digestBuilder = StringBuilder()
        digestBuilder.append("📰 Daily News Digest — NewsPulse\n")
        digestBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")

        topArticles.forEachIndexed { index, item ->
            digestBuilder.append("${index + 1}. [${item.source}] ${item.title}\n")
            if (item.link.isNotBlank()) {
                digestBuilder.append("   🔗 ${item.link}\n")
            }
            digestBuilder.append("\n")
        }

        digestBuilder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
        digestBuilder.append("Stay informed with NewsPulse App")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "NewsPulse Daily Digest")
            putExtra(Intent.EXTRA_TEXT, digestBuilder.toString())
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share Daily Digest via"))
    }

    private fun showArticleDetailDialog(article: NewsArticle) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogArticleDetailBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialogBinding.dialogChannelBadge.text = article.source
        try {
            dialogBinding.dialogChannelBadge.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(article.getSourceColorHex()))
        } catch (_: Exception) {
            dialogBinding.dialogChannelBadge.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.badge_general))
        }

        dialogBinding.dialogCategory.text = article.category
        dialogBinding.dialogPubTime.text = article.getFormattedTime()
        dialogBinding.dialogTitle.text = article.title
        dialogBinding.dialogDescription.text = article.description

        dialogBinding.btnOpenBrowser.setOnClickListener {
            if (article.link.isNotBlank()) {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "No external link available", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBinding.dialogBtnShare.setOnClickListener {
            shareArticle(article)
        }

        dialogBinding.dialogBtnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
