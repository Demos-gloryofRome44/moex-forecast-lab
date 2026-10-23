package ru.quotelab.usecase.common

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class JobLock {
    private val lock = ReentrantLock()

    fun <T> exclusive(block: () -> T): T = lock.withLock(block)

    val isBusy: Boolean get() = lock.isLocked
}
