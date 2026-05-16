package com.example.plantreminder.domain.models

data class PottingMix(
    val components: List<PottingMixComponent>,
    val notes: List<String>,
)

data class PottingMixComponent(
    val material: String,
    val percentage: Int,
)
