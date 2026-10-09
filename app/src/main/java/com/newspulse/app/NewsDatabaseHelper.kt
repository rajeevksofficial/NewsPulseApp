package com.newspulse.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.concurrent.TimeUnit

class NewsDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "news_pulse.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_ARTICLES = "articles"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_LINK = "link"
        const val COLUMN_SOURCE = "source"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_PUB_DATE = "pub_date"
        const val COLUMN_IMAGE_URL = "image_url"
        const val COLUMN_IS_BOOKMARKED = "is_bookmarked"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_ARTICLES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT UNIQUE NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_LINK TEXT,
                $COLUMN_SOURCE TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_PUB_DATE INTEGER NOT NULL,
                $COLUMN_IMAGE_URL TEXT,
                $COLUMN_IS_BOOKMARKED INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
        seedInitialNews(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ARTICLES")
        onCreate(db)
    }

    private fun seedInitialNews(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val seeds = listOf(
            // Times of India
            NewsArticle(
                title = "India's Tech Sector Poised for Historic Surge with New AI Semiconductor Initiatives",
                description = "Government and global silicon leaders unveil multi-billion dollar manufacturing fabs and semiconductor design clusters across Gujarat, Karnataka, and Tamil Nadu.",
                link = "https://timesofindia.indiatimes.com/business/india-business/tech-semiconductor-expansion/articleshow/98765432.cms",
                source = "Times of India",
                category = "Technology",
                pubDate = now - TimeUnit.MINUTES.toMillis(15)
            ),
            NewsArticle(
                title = "ISRO Finalizes Humanoid Flight Preparations Ahead of Landmark Gaganyaan Orbital Mission",
                description = "Space agency completes rigorous cryogenic propulsion testing and capsule recovery drills in the Arabian Sea, ensuring all parameters are primed for the human spaceflight program.",
                link = "https://timesofindia.indiatimes.com/india/isro-gaganyaan-space-mission-update/articleshow/98765433.cms",
                source = "Times of India",
                category = "Science & Space",
                pubDate = now - TimeUnit.MINUTES.toMillis(50)
            ),
            NewsArticle(
                title = "National Expressway Network Expands with 6-Lane Smart Green Freight Corridors",
                description = "High-speed highway corridor cuts transit duration between key industrial hubs by over 45%, featuring automated solar tolling and wildlife underpasses.",
                link = "https://timesofindia.indiatimes.com/india/national-highways-expressway-network/articleshow/98765434.cms",
                source = "Times of India",
                category = "Infrastructure",
                pubDate = now - TimeUnit.HOURS.toMillis(2)
            ),

            // Hindustan Times
            NewsArticle(
                title = "Stock Markets Surge to Fresh Record Highs as Foreign Investments and Auto Sales Rally",
                description = "Sensex and Nifty indices surge to unprecedented milestones driven by banking resilience, surging consumer demand, and robust quarterly manufacturing output.",
                link = "https://www.hindustantimes.com/business/sensex-nifty-record-highs-market-rally-101700000000.html",
                source = "Hindustan Times",
                category = "Economy",
                pubDate = now - TimeUnit.MINUTES.toMillis(25)
            ),
            NewsArticle(
                title = "Clean Energy Benchmark: Solar and Wind Capacity Account for 60% of New Grid Power",
                description = "India achieves ambitious renewable milestones years ahead of targeted timelines, driven by massive utility-scale solar parks and offshore wind developments.",
                link = "https://www.hindustantimes.com/india-news/green-energy-solar-wind-transition-101700000001.html",
                source = "Hindustan Times",
                category = "Environment",
                pubDate = now - TimeUnit.MINUTES.toMillis(75)
            ),
            NewsArticle(
                title = "National Education Reform Empowers Next-Gen Coding and Robotics Across 10,000 Schools",
                description = "Upgraded vocational curriculums introduce hands-on artificial intelligence, design thinking, and practical STEM modules for middle school students.",
                link = "https://www.hindustantimes.com/education/stem-curriculum-ai-schools-101700000002.html",
                source = "Hindustan Times",
                category = "Education",
                pubDate = now - TimeUnit.HOURS.toMillis(3)
            ),

            // NDTV News
            NewsArticle(
                title = "Digital Public Infrastructure Model Adopted by Global Economies for Financial Inclusion",
                description = "International financial institutions praise unified payments and digital identity stacks as transformative models for developing financial ecosystems globally.",
                link = "https://www.ndtv.com/india-news/india-digital-public-infrastructure-global-recognition-5123456",
                source = "NDTV News",
                category = "Governance",
                pubDate = now - TimeUnit.MINUTES.toMillis(35)
            ),
            NewsArticle(
                title = "Champions Trophy Tour: Resilient Team India Squad Announced with Dynamic Young Talent",
                description = "Selectors unveil a balanced combination of aggressive top-order batters, express pace bowlers, and seasoned all-rounders ahead of the marquee international tournament.",
                link = "https://sports.ndtv.com/cricket/champions-trophy-squad-announcement-india-5123457",
                source = "NDTV News",
                category = "Sports",
                pubDate = now - TimeUnit.HOURS.toMillis(1)
            ),
            NewsArticle(
                title = "Monsoon Agricultural Output Surpasses Projections, Bolstering Rural Incomes",
                description = "Adequate water reservoir levels and enhanced drip irrigation infrastructure yield record foodgrain harvests across northern and central agricultural belts.",
                link = "https://www.ndtv.com/business/rural-economy-monsoon-harvest-update-5123458",
                source = "NDTV News",
                category = "Agriculture",
                pubDate = now - TimeUnit.HOURS.toMillis(4)
            ),

            // The Hindu
            NewsArticle(
                title = "Supreme Court Delivers Unanimous Ruling Upholding Citizen Privacy and Data Protection",
                description = "Constitution bench issues guidelines restricting unauthorized surveillance and enforcing strict consent safeguards for digital platforms handling personal records.",
                link = "https://www.thehindu.com/news/national/supreme-court-data-privacy-ruling/article68000001.ece",
                source = "The Hindu",
                category = "National",
                pubDate = now - TimeUnit.MINUTES.toMillis(40)
            ),
            NewsArticle(
                title = "Archeological Survey Excavates Ancient Maritime Harbor in Coastal Gujarat",
                description = "Excavations uncover sophisticated dockyard stonework, terracotta seals, and trade ceramics indicating vibrant overseas commerce dating back over two millennia.",
                link = "https://www.thehindu.com/news/national/gujarat-ancient-port-excavation/article68000002.ece",
                source = "The Hindu",
                category = "Heritage",
                pubDate = now - TimeUnit.HOURS.toMillis(5)
            ),

            // BBC News
            NewsArticle(
                title = "Global Clean Energy Investments Eclipse Fossil Fuel Financing for Second Consecutive Year",
                description = "International Energy Agency report highlights historic capital rotation into battery storage, solar farms, electric mobility, and grid modernizations.",
                link = "https://www.bbc.com/news/world-asia-india-68900001",
                source = "BBC News",
                category = "Global",
                pubDate = now - TimeUnit.HOURS.toMillis(2)
            ),
            NewsArticle(
                title = "Quantum Computer Breakthrough: 1,000-Qubit Processor Solves Complex Molecular Chemistry",
                description = "Scientists demonstrate fault-tolerant quantum algorithms capable of accelerating life-saving drug discovery and superconductor material discovery.",
                link = "https://www.bbc.com/news/technology-68900002",
                source = "BBC News",
                category = "Technology",
                pubDate = now - TimeUnit.HOURS.toMillis(6)
            )
        )

        for (item in seeds) {
            val cv = ContentValues().apply {
                put(COLUMN_TITLE, item.title)
                put(COLUMN_DESCRIPTION, item.description)
                put(COLUMN_LINK, item.link)
                put(COLUMN_SOURCE, item.source)
                put(COLUMN_CATEGORY, item.category)
                put(COLUMN_PUB_DATE, item.pubDate)
                put(COLUMN_IMAGE_URL, item.imageUrl)
                put(COLUMN_IS_BOOKMARKED, 0)
            }
            db.insertWithOnConflict(TABLE_ARTICLES, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    fun insertArticles(articles: List<NewsArticle>): Int {
        val db = writableDatabase
        var insertedCount = 0
        db.beginTransaction()
        try {
            for (article in articles) {
                val cv = ContentValues().apply {
                    put(COLUMN_TITLE, article.title)
                    put(COLUMN_DESCRIPTION, article.description)
                    put(COLUMN_LINK, article.link)
                    put(COLUMN_SOURCE, article.source)
                    put(COLUMN_CATEGORY, article.category)
                    put(COLUMN_PUB_DATE, article.pubDate)
                    put(COLUMN_IMAGE_URL, article.imageUrl)
                }
                val result = db.insertWithOnConflict(TABLE_ARTICLES, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                if (result != -1L) {
                    insertedCount++
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return insertedCount
    }

    fun getArticles(
        sourceFilter: String? = null,
        bookmarkedOnly: Boolean = false,
        searchQuery: String? = null
    ): List<NewsArticle> {
        val list = mutableListOf<NewsArticle>()
        val db = readableDatabase

        val whereClauses = mutableListOf<String>()
        val whereArgs = mutableListOf<String>()

        if (!sourceFilter.isNullOrBlank() && sourceFilter != "All Feeds") {
            whereClauses.add("$COLUMN_SOURCE = ?")
            whereArgs.add(sourceFilter)
        }

        if (bookmarkedOnly) {
            whereClauses.add("$COLUMN_IS_BOOKMARKED = 1")
        }

        if (!searchQuery.isNullOrBlank()) {
            whereClauses.add("($COLUMN_TITLE LIKE ? OR $COLUMN_DESCRIPTION LIKE ?)")
            whereArgs.add("%$searchQuery%")
            whereArgs.add("%$searchQuery%")
        }

        val selection = if (whereClauses.isNotEmpty()) whereClauses.joinToString(" AND ") else null
        val selectionArgs = if (whereArgs.isNotEmpty()) whereArgs.toTypedArray() else null

        val cursor = db.query(
            TABLE_ARTICLES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_PUB_DATE DESC"
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COLUMN_ID)
                val titleIdx = it.getColumnIndexOrThrow(COLUMN_TITLE)
                val descIdx = it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)
                val linkIdx = it.getColumnIndexOrThrow(COLUMN_LINK)
                val sourceIdx = it.getColumnIndexOrThrow(COLUMN_SOURCE)
                val catIdx = it.getColumnIndexOrThrow(COLUMN_CATEGORY)
                val dateIdx = it.getColumnIndexOrThrow(COLUMN_PUB_DATE)
                val imgIdx = it.getColumnIndexOrThrow(COLUMN_IMAGE_URL)
                val bookmarkIdx = it.getColumnIndexOrThrow(COLUMN_IS_BOOKMARKED)

                do {
                    list.add(
                        NewsArticle(
                            id = it.getLong(idIdx),
                            title = it.getString(titleIdx),
                            description = it.getString(descIdx) ?: "",
                            link = it.getString(linkIdx) ?: "",
                            source = it.getString(sourceIdx),
                            category = it.getString(catIdx),
                            pubDate = it.getLong(dateIdx),
                            imageUrl = it.getString(imgIdx) ?: "",
                            isBookmarked = it.getInt(bookmarkIdx) == 1
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }

    fun toggleBookmark(articleId: Long, isBookmarked: Boolean): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_IS_BOOKMARKED, if (isBookmarked) 1 else 0)
        }
        val affected = db.update(TABLE_ARTICLES, cv, "$COLUMN_ID = ?", arrayOf(articleId.toString()))
        return affected > 0
    }

    fun getTopHeadlinesForDigest(limit: Int = 5): List<NewsArticle> {
        val db = readableDatabase
        val list = mutableListOf<NewsArticle>()
        val cursor = db.query(
            TABLE_ARTICLES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_PUB_DATE DESC",
            limit.toString()
        )
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COLUMN_ID)
                val titleIdx = it.getColumnIndexOrThrow(COLUMN_TITLE)
                val descIdx = it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)
                val linkIdx = it.getColumnIndexOrThrow(COLUMN_LINK)
                val sourceIdx = it.getColumnIndexOrThrow(COLUMN_SOURCE)
                val catIdx = it.getColumnIndexOrThrow(COLUMN_CATEGORY)
                val dateIdx = it.getColumnIndexOrThrow(COLUMN_PUB_DATE)
                val imgIdx = it.getColumnIndexOrThrow(COLUMN_IMAGE_URL)
                val bookmarkIdx = it.getColumnIndexOrThrow(COLUMN_IS_BOOKMARKED)

                do {
                    list.add(
                        NewsArticle(
                            id = it.getLong(idIdx),
                            title = it.getString(titleIdx),
                            description = it.getString(descIdx) ?: "",
                            link = it.getString(linkIdx) ?: "",
                            source = it.getString(sourceIdx),
                            category = it.getString(catIdx),
                            pubDate = it.getLong(dateIdx),
                            imageUrl = it.getString(imgIdx) ?: "",
                            isBookmarked = it.getInt(bookmarkIdx) == 1
                        )
                    )
                } while (it.moveToNext())
            }
        }
        return list
    }
}
