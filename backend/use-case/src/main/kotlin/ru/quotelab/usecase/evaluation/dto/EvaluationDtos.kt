package ru.quotelab.usecase.evaluation.dto

data class EvaluationRunReport(
    val checked: Int,
    val evaluated: Int,
    val waitingForTarget: Int,
)
