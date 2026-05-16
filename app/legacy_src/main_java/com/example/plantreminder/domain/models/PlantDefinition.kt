package com.example.plantreminder.domain.models

import java.time.LocalDate

data class PlantDefinition(
    val id: String,
    val nombre: String,
    val especie: String?,
    val activada: Boolean,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate?,
    val responsable: String,
    val mostrarEnSiembra: Boolean,
    val mesesSiembra: List<Int>,
    val mesesRecoleccion: List<Int>,
    val riego: WateringRule?,
    val abono: Map<Season, List<FertilizerRule>>,
    val exposicionSolar: String?,
    val interior: Boolean,
    val notas: List<String>,
    val composicionMaceta: PottingMix?,
    val fuenteInformacionUrl: String?,
    val fuenteSustratoUrl: String?,
    val plagas: PestMonitoringRule?,
    val metadata: Map<String, String>,
    val rawPayload: String?,
)
