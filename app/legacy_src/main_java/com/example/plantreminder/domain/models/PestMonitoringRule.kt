package com.example.plantreminder.domain.models

data class PestMonitoringRule(
    val reviewEveryDays: Int?,
    val commonIssues: List<String>,
    val notes: List<String>,
)
