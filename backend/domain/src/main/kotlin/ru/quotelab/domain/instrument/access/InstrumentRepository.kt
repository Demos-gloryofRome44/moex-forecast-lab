package ru.quotelab.domain.instrument.access

import ru.quotelab.domain.instrument.Instrument
import ru.quotelab.domain.instrument.valueobject.Secid

interface InstrumentRepository {
    fun findAll(): List<Instrument>
    fun findActive(): List<Instrument>
    fun findBySecid(secid: Secid): Instrument?
    fun save(instrument: Instrument): Instrument
}
