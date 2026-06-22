package com.example.mmapp.app4.domain.scripts

import com.example.mmapp.app4.data.centros.CentrosCsvAssetDataSource
import com.example.mmapp.app4.data.centros.CentrosCsvWriter
import com.example.mmapp.app4.data.centros.CentrosOsrmClient
import com.example.mmapp.app4.data.centros.CentrosProcessingService
import com.example.mmapp.app4.domain.models.ScriptDefinition
import com.example.mmapp.app4.domain.models.ScriptExecutionResult
import com.example.mmapp.app4.domain.models.ScriptLogEntry
import com.example.mmapp.app4.domain.models.ScriptLogLevel
import com.example.mmapp.app4.domain.models.ScriptResultItem
import com.example.mmapp.settings.data.repositories.AppSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class GenerateSchoolDriveTimesScript(
    private val assetDataSource: CentrosCsvAssetDataSource,
    private val appSettingsRepository: AppSettingsRepository,
    private val processingService: CentrosProcessingService,
    private val osrmClient: CentrosOsrmClient,
    private val csvWriter: CentrosCsvWriter,
) : ScriptTask {
    override val definition: ScriptDefinition = ScriptDefinition(
        id = AppSettingsRepository.SCHOOL_DRIVE_TIME_PROCESS_ID,
        topic = "Centros",
        title = "Calcular tiempos en coche",
        description = "Filtra institutos públicos, calcula distancia y tiempo en coche desde un origen y genera un CSV en Descargas.",
    )

    override suspend fun execute(
        onLog: (ScriptLogEntry) -> Unit,
    ): ScriptExecutionResult = withContext(Dispatchers.IO) {
        val logs = mutableListOf<ScriptLogEntry>()

        fun emit(
            message: String,
            level: ScriptLogLevel = ScriptLogLevel.INFO,
        ) {
            val entry = ScriptLogEntry(
                message = message,
                level = level,
            )
            logs += entry
            onLog(entry)
        }

        emit("Ejecutando proceso: ${definition.title}")

        runCatching {
            val settings = appSettingsRepository.getProcessSettings()
                .firstOrNull { it.processId == definition.id }
                ?: error("No se encontró la configuración del proceso.")

            val config = processingService.buildProcessConfig(
                latitudeRaw = settings.latitude,
                longitudeRaw = settings.longitude,
                outputFileNameRaw = settings.outputFileName,
            )
            emit(
                message = "Parámetros: lat=${config.latitude}, lon=${config.longitude}, salida=${config.outputFileName}",
            )

            val sourceRows = assetDataSource.loadRows()
            emit(
                message = "Centros cargados desde assets: ${sourceRows.size}",
            )

            val eligibleRows = processingService.filterEligibleSchools(sourceRows)
            emit(
                message = "Centros tras filtros: ${eligibleRows.size}",
            )

            val baseRows = processingService.toDriveTimeRows(
                rows = eligibleRows,
                originLatitude = config.latitude,
                originLongitude = config.longitude,
            )
            emit("Iniciando consultas a OSRM. Esto puede tardar varios minutos.")

            val resolvedRows = baseRows.mapIndexed { index, row ->
                emit(
                    message = "Procesando ${index + 1}/${baseRows.size}: ${row.denominacion} (${row.localidad})",
                )
                val driveTime = runCatching {
                    osrmClient.fetchDriveTimeMinutes(
                        originLatitude = config.latitude,
                        originLongitude = config.longitude,
                        destinationLatitude = row.latitud,
                        destinationLongitude = row.longitud,
                    )
                }.getOrNull()

                emit(
                    message = driveTime?.let {
                        "Tiempo calculado para ${row.denominacion}: ${"%.1f".format(it)} min"
                    } ?: "OSRM sin dato para ${row.denominacion}",
                )

                if ((index + 1) % 25 == 0 || index == baseRows.lastIndex) {
                    emit("Procesados ${index + 1}/${baseRows.size} centros")
                }

                if (index < baseRows.lastIndex) {
                    emit("Esperando 1s antes de la siguiente petición")
                    delay(OSRM_DELAY_MILLIS)
                }
                processingService.withDriveTime(row, driveTime)
            }

            val sortedRows = processingService.sortRows(resolvedRows)
            val writeResult = csvWriter.write(
                fileName = config.outputFileName,
                rows = sortedRows,
            )
            emit(
                message = "CSV generado en ${writeResult.displayLocation}",
                level = ScriptLogLevel.SUCCESS,
            )

            val items = sortedRows.take(10).map { row ->
                ScriptResultItem(
                    title = "${row.codigo} - ${row.denominacion}",
                    detail = buildString {
                        append(row.localidad)
                        append(" | ")
                        append("distancia ${"%.2f".format(row.distanciaKm)} km")
                        append(" | ")
                        append(row.tiempoMinutosCoche?.let { "coche ${"%.1f".format(it)} min" } ?: "coche sin dato")
                    },
                    linkUrl = null,
                )
            }

            val unresolvedCount = sortedRows.count { it.tiempoMinutosCoche == null }
            ScriptExecutionResult(
                summary = "CSV generado con ${sortedRows.size} centros. Sin tiempo OSRM: $unresolvedCount.",
                logs = logs,
                items = items,
            )
        }.getOrElse { throwable ->
            emit(
                message = "Error: ${throwable.message ?: "Fallo desconocido"}",
                level = ScriptLogLevel.ERROR,
            )
            ScriptExecutionResult(
                summary = "El script ha fallado.",
                logs = logs,
                items = emptyList(),
            )
        }
    }

    companion object {
        private const val OSRM_DELAY_MILLIS = 1_000L
    }
}
