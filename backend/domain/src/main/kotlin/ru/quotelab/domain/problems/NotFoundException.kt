package ru.quotelab.domain.problems

abstract class NotFoundException(message: String) : RuntimeException(message)

class InstrumentNotFoundException(secid: String) :
    NotFoundException("Instrument '$secid' not found")

class SecurityNotFoundException(secid: String) :
    NotFoundException("Security '$secid' not found on MOEX")

class StrategyNotFoundException(id: String) :
    NotFoundException("Strategy '$id' not found")

class ForecastNotFoundException(id: String) :
    NotFoundException("Forecast '$id' not found")
