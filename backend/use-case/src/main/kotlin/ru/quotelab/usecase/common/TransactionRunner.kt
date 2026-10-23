package ru.quotelab.usecase.common

interface TransactionRunner {
    fun <T> inTransaction(block: () -> T): T
}
