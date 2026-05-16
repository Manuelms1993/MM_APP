package com.example.mmapp.app1.domain.models

data class PestMonitoringRule(
    val reviewEveryDays: Int?,
    val commonIssues: List<String>,
    val notes: List<String>,
)
