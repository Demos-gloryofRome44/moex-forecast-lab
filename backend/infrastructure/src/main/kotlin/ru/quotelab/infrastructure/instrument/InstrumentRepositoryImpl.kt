package ru.quotelab.infrastructure.instrument

import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.access.InstrumentRepository
import ru.quotelab.domain.instrument.valueobject.Secid
import ru.quotelab.infrastructure.instrument.mapper.toDomain
import ru.quotelab.infrastructure.instrument.mapper.toEntity
import ru.tinkoff.kora.common.Component

@Component
class InstrumentRepositoryImpl(
    private val dao: InstrumentDao,
) : InstrumentRepository {
    override fun findAll(): List<Instrument> = dao.findAll().map { it.toDomain() }

    override fun findActive(): List<Instrument> = dao.findActive().map { it.toDomain() }

    override fun findBySecid(secid: Secid): Instrument? = dao.findBySecid(secid.value)?.toDomain()

    override fun save(instrument: Instrument): Instrument {
        dao.upsert(instrument.toEntity())
        return instrument
    }
}
