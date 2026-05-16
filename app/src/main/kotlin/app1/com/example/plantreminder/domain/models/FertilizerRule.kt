package com.example.mmapp.app1.domain.models

data class FertilizerRule(
    val tipo: String,
    val cadaDias: Int,
    val cantidad: String?,
    val dosis: FertilizerDoseMode,
    val dosisTextoOriginal: String?,
    val recordatorio: String?,
)

