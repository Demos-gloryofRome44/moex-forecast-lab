package ru.quotelab.domain.news.access

import ru.quotelab.domain.news.NewsItem
import java.time.Instant

interface NewsProvider {
    fun fetchSince(since: Instant): List<NewsItem>
}
