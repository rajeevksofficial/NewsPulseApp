package com.newspulse.app

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object RssFeedParser {

    private val dateFormats = arrayOf(
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
        SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    )

    fun parse(inputStream: InputStream, sourceName: String, defaultCategory: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")

            var eventType = parser.eventType
            var inItem = false
            var title = ""
            var description = ""
            var link = ""
            var pubDateStr = ""
            var category = defaultCategory
            var imageUrl = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name?.lowercase(Locale.ROOT)

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName == "item" || tagName == "entry") {
                            inItem = true
                            title = ""
                            description = ""
                            link = ""
                            pubDateStr = ""
                            category = defaultCategory
                            imageUrl = ""
                        } else if (inItem) {
                            when (tagName) {
                                "title" -> title = parser.nextText().trim()
                                "description", "summary" -> description = parser.nextText().trim()
                                "link" -> {
                                    val href = parser.getAttributeValue(null, "href")
                                    link = if (!href.isNullOrBlank()) href else parser.nextText().trim()
                                }
                                "pubdate", "published", "updated" -> pubDateStr = parser.nextText().trim()
                                "category" -> {
                                    val cat = parser.nextText().trim()
                                    if (cat.isNotBlank()) category = cat
                                }
                                "enclosure" -> {
                                    val type = parser.getAttributeValue(null, "type") ?: ""
                                    if (type.startsWith("image")) {
                                        imageUrl = parser.getAttributeValue(null, "url") ?: ""
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName == "item" || tagName == "entry") {
                            if (title.isNotBlank()) {
                                val cleanDesc = cleanHtml(description)
                                val pubTimestamp = parseDate(pubDateStr)
                                articles.add(
                                    NewsArticle(
                                        title = cleanHtml(title),
                                        description = cleanDesc.ifBlank { "Read the full breaking story on $sourceName." },
                                        link = link,
                                        source = sourceName,
                                        category = category,
                                        pubDate = pubTimestamp,
                                        imageUrl = imageUrl
                                    )
                                )
                            }
                            inItem = false
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return articles
    }

    fun cleanHtml(raw: String): String {
        if (raw.isBlank()) return ""
        // Remove HTML tags
        var cleaned = raw.replace(Regex("<[^>]*>"), " ")
        // Replace common HTML entities
        cleaned = cleaned.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
        return cleaned
    }

    fun parseDate(dateStr: String): Long {
        if (dateStr.isBlank()) return System.currentTimeMillis()
        for (format in dateFormats) {
            try {
                val parsed = format.parse(dateStr.trim())
                if (parsed != null) return parsed.time
            } catch (_: Exception) {
            }
        }
        return System.currentTimeMillis()
    }
}
