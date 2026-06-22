package com.example.mmapp.app4.data.centros

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class CentrosProcessingService {
    fun buildProcessConfig(
        latitudeRaw: String?,
        longitudeRaw: String?,
        outputFileNameRaw: String?,
    ): SchoolDriveTimeProcessConfig {
        val latitude = latitudeRaw?.trim()?.toDoubleOrNull()
            ?: error("La latitud del proceso no es válida.")
        val longitude = longitudeRaw?.trim()?.toDoubleOrNull()
            ?: error("La longitud del proceso no es válida.")
        return SchoolDriveTimeProcessConfig(
            latitude = latitude,
            longitude = longitude,
            outputFileName = normalizeOutputFileName(outputFileNameRaw),
        )
    }

    fun filterEligibleSchools(rows: List<CentroCsvRow>): List<CentroCsvRow> = rows.filter { row ->
        row.regimen == "PÚB." && row.denominacionGenericaEs == "INSTITUTO DE EDUCACIÓN SECUNDARIA"
    }

    fun toDriveTimeRows(
        rows: List<CentroCsvRow>,
        originLatitude: Double,
        originLongitude: Double,
    ): List<CentroDriveTimeRow> = rows.map { row ->
        CentroDriveTimeRow(
            codigo = row.codigo,
            denominacion = row.denominacion,
            regimen = row.regimen,
            direccion = row.direccion,
            longitud = row.longitud,
            latitud = row.latitud,
            provincia = row.provincia,
            localidad = row.localidad,
            distanciaKm = haversineKm(originLatitude, originLongitude, row.latitud, row.longitud),
            tiempoMinutosCoche = null,
        )
    }

    fun withDriveTime(
        row: CentroDriveTimeRow,
        driveTimeMinutes: Double?,
    ): CentroDriveTimeRow = row.copy(tiempoMinutosCoche = driveTimeMinutes)

    fun sortRows(rows: List<CentroDriveTimeRow>): List<CentroDriveTimeRow> = rows.sortedWith(
        compareBy<CentroDriveTimeRow> { it.tiempoMinutosCoche == null }
            .thenBy { it.tiempoMinutosCoche ?: Double.MAX_VALUE }
            .thenBy { it.distanciaKm },
    )

    fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val earthRadiusKm = 6371.0
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
            cos(phi1) * cos(phi2) * sin(deltaLambda / 2) * sin(deltaLambda / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun normalizeOutputFileName(rawValue: String?): String {
        val candidate = rawValue
            ?.trim()
            ?.replace(Regex("""[\\/:*?"<>|]"""), "_")
            ?.replace(Regex("\\s+"), "_")
            .orEmpty()
            .ifBlank { "centros_final" }

        return if (candidate.endsWith(".csv", ignoreCase = true)) candidate else "$candidate.csv"
    }
}
