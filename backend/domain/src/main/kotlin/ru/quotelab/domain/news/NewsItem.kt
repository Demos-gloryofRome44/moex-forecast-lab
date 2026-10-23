package ru.quotelab.domain.news

import ru.quotelab.domain.instrument.valueobject.Secid
import java.time.Instant

data class NewsItem(
    val source: String,
    val url: String,
    val title: String,
    val publishedAt: Instant,
    val secids: Set<Secid>,
    val sentiment: Double?,
)
