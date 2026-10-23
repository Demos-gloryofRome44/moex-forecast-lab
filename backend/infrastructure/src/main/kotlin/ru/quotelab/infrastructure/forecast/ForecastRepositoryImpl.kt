package ru.quotelab.infrastructure.forecast

import ru.quotelab.domain.common.Page
import ru.quotelab.domain.common.PageRequest
import ru.quotelab.domain.forecast.Forecast
import ru.quotelab.domain.forecast.ForecastOrigin
import ru.quotelab.domain.forecast.access.ForecastFilter
import ru.quotelab.domain.forecast.access.ForecastRepository
import ru.quotelab.domain.forecast.valueobject.ForecastId
import ru.quotelab.infrastructure.forecast.mapper.toDomain
import ru.quotelab.infrastructure.forecast.mapper.toEntity
import ru.tinkoff.kora.common.Component

@Component
class ForecastRepositoryImpl(
    private val dao: ForecastDao,
) : ForecastRepository {
    override fun insertIfAbsent(forecast: Forecast): Boolean = dao.insertIfAbsent(forecast.toEntity()).value > 0

    override fun findById(id: ForecastId): Forecast? = dao.findById(id.value)?.toDomain()

    override fun findPending(origin: ForecastOrigin, limit: Int, afterId: ForecastId?): List<Forecast> =
        dao.findPending(origin.name, limit, afterId?.value).map { it.toDomain() }

    override fun markEvaluated(id: ForecastId) {
        dao.markEvaluated(id.value)
    }

    override fun search(filter: ForecastFilter, page: PageRequest): Page<Forecast> {
        val items = with(filter) {
            dao.search(
                secid?.value, strategyId?.value, status?.name, horizon?.days, origin?.name, from, till,
                page.size, page.offset,
            )
        }
        val total = with(filter) {
            dao.count(secid?.value, strategyId?.value, status?.name, horizon?.days, origin?.name, from, till)
        }
        return Page(items.map { it.toDomain() }, page.page, page.size, total)
    }
}
