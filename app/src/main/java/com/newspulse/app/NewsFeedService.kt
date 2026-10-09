package com.newspulse.app

import android.os.Handler
import android.os.Looper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object NewsFeedService {

    data class FeedEndpoint(
        val source: String,
        val category: String,
        val url: String
    )

    private val endpoints = listOf(
        FeedEndpoint(
            source = "Times of India",
            category = "Top Stories",
            url = "https://timesofindia.indiatimes.com/rssfeedstopstories.cms"
        ),
        FeedEndpoint(
            source = "Hindustan Times",
            category = "Top News",
            url = "https://www.hindustantimes.com/feeds/rss/topnews/rssfeed.xml"
        ),
        FeedEndpoint(
            source = "NDTV News",
            category = "Top Stories",
            url = "https://feeds.feedburner.com/ndtvnews-top-stories"
        ),
        FeedEndpoint(
            source = "The Hindu",
            category = "National",
            url = "https://www.thehindu.com/news/national/feeder/default.rss"
        ),
        FeedEndpoint(
            source = "BBC News",
            category = "India & World",
            url = "https://feeds.bbci.co.uk/news/world/asia/india/rss.xml"
        )
    )

    private val executor = Executors.newFixedThreadPool(4)
    private val mainHandler = Handler(Looper.getMainLooper())

    fun fetchAllFeeds(onComplete: (List<NewsArticle>, Boolean) -> Unit) {
        executor.execute {
            val combinedArticles = mutableListOf<NewsArticle>()
            var anySuccess = false

            for (endpoint in endpoints) {
                try {
                    val articles = fetchSingleFeed(endpoint.url, endpoint.source, endpoint.category)
                    if (articles.isNotEmpty()) {
                        combinedArticles.addAll(articles)
                        anySuccess = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            combinedArticles.sortByDescending { it.pubDate }

            mainHandler.post {
                onComplete(combinedArticles, anySuccess)
            }
        }
    }

    private fun fetchSingleFeed(urlString: String, source: String, category: String): List<NewsArticle> {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) NewsPulse/1.0")
                setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml, */*")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
                RssFeedParser.parse(inputStream, source, category)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        } finally {
            try {
                inputStream?.close()
                connection?.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}
