package com.example.mmapp.app4.data.centros

data class CentroCsvRow(
    val codigo: String,
    val denominacionGenericaEs: String,
    val denominacion: String,
    val regimen: String,
    val direccion: String,
    val longitud: Double,
    val latitud: Double,
    val provincia: String,
    val localidad: String,
)

data class CentroDriveTimeRow(
    val codigo: String,
    val denominacion: String,
    val regimen: String,
    val direccion: String,
    val longitud: Double,
    val latitud: Double,
    val provincia: String,
    val localidad: String,
    val distanciaKm: Double,
    val tiempoMinutosCoche: Double?,
)

data class SchoolDriveTimeProcessConfig(
    val latitude: Double,
    val longitude: Double,
    val outputFileName: String,
)

data class CsvWriteResult(
    val displayLocation: String,
    val uriString: String? = null,
)
