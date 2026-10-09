package com.newspulse.app

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class NewsArticle(
    val id: Long = 0,
    val title: String,
    val description: String,
    val link: String,
    val source: String,
    val category: String = "Top Stories",
    val pubDate: Long = System.currentTimeMillis(),
    val imageUrl: String = "",
    var isBookmarked: Boolean = false
) {
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - pubDate

        return when {
            diff < 0 -> "Just now"
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            else -> {
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                sdf.format(Date(pubDate))
            }
        }
    }

    fun getSourceColorHex(): String {
        return when (source) {
            "Times of India" -> "#D32F2F"
            "Hindustan Times" -> "#0288D1"
            "NDTV News" -> "#F57C00"
            "The Hindu" -> "#3F51B5"
            "BBC News" -> "#C2185B"
            else -> "#00897B"
        }
    }

    fun getShareableText(): String {
        return """
            📰 Breaking News: $title
            
            🏷️ Source: $source ($category)
            ⏱️ $description
            
            🔗 Read full article: $link
            
            Shared via NewsPulse App
        """.trimIndent()
    }
}
