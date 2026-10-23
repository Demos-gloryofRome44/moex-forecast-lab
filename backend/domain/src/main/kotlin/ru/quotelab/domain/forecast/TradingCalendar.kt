package ru.quotelab.domain.forecast

import java.time.DayOfWeek
import java.time.LocalDate

object TradingCalendar {
    fun addWeekdays(from: LocalDate, days: Int): LocalDate {
        var date = from
        var left = days
        while (left > 0) {
            date = date.plusDays(1)
            if (isWeekday(date)) left--
        }
        return date
    }

    fun isWeekday(date: LocalDate): Boolean =
        date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY
}
