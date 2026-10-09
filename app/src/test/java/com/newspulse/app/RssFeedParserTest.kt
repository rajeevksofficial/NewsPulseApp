package com.newspulse.app

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class RssFeedParserTest {

    @Test
    fun testCleanHtmlRemovesTagsAndEntities() {
        val rawHtml = "<p>India &amp; France sign <b>landmark</b> agreement.&nbsp;Read &quot;more&quot; here!</p>"
        val cleaned = RssFeedParser.cleanHtml(rawHtml)
        assertEquals("India & France sign landmark agreement. Read \"more\" here!", cleaned)
    }

    @Test
    fun testCleanHtmlWithSpecialCharacters() {
        val raw = "<div>Breaking: Market closes at record 84,000&#39;s peak &lt;exclusive&gt;</div>"
        val cleaned = RssFeedParser.cleanHtml(raw)
        assertEquals("Breaking: Market closes at record 84,000's peak <exclusive>", cleaned)
    }

    @Test
    fun testParseDateRFC822() {
        val rfcDate = "Fri, 09 Oct 2026 12:00:00 GMT"
        val parsed = RssFeedParser.parseDate(rfcDate)
        assertTrue(parsed > 0)
    }

    @Test
    fun testParseDateEmptyFallsBackToNow() {
        val before = System.currentTimeMillis() - 1000
        val parsed = RssFeedParser.parseDate("")
        val after = System.currentTimeMillis() + 1000
        assertTrue(parsed in before..after)
    }

    @Test
    fun testArticleFormattingAndShareText() {
        val now = System.currentTimeMillis()
        val article = NewsArticle(
            title = "Historic Space Launch",
            description = "ISRO successfully puts satellites into orbit.",
            link = "https://example.com/story",
            source = "Times of India",
            category = "Science",
            pubDate = now - TimeUnit.MINUTES.toMillis(5)
        )

        assertEquals("5m ago", article.getFormattedTime())
        assertEquals("#D32F2F", article.getSourceColorHex())
        assertTrue(article.getShareableText().contains("Historic Space Launch"))
        assertTrue(article.getShareableText().contains("Times of India"))
        assertTrue(article.getShareableText().contains("https://example.com/story"))
    }

    @Test
    fun testChannelColorMappings() {
        val toi = NewsArticle(title = "T1", description = "D1", link = "L1", source = "Times of India")
        val ht = NewsArticle(title = "T2", description = "D2", link = "L2", source = "Hindustan Times")
        val ndtv = NewsArticle(title = "T3", description = "D3", link = "L3", source = "NDTV News")
        val hindu = NewsArticle(title = "T4", description = "D4", link = "L4", source = "The Hindu")
        val bbc = NewsArticle(title = "T5", description = "D5", link = "L5", source = "BBC News")

        assertEquals("#D32F2F", toi.getSourceColorHex())
        assertEquals("#0288D1", ht.getSourceColorHex())
        assertEquals("#F57C00", ndtv.getSourceColorHex())
        assertEquals("#3F51B5", hindu.getSourceColorHex())
        assertEquals("#C2185B", bbc.getSourceColorHex())
    }
}
