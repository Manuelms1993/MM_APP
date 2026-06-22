package com.example.mmapp.app4.data.centros

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

class CentrosCsvWriter(
    private val context: Context,
) {
    fun write(
        fileName: String,
        rows: List<CentroDriveTimeRow>,
    ): CsvWriteResult {
        val csv = buildCsv(rows)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeWithMediaStore(fileName, csv)
        } else {
            writeToAppDownloads(fileName, csv)
        }
    }

    private fun writeWithMediaStore(
        fileName: String,
        csv: String,
    ): CsvWriteResult {
        val resolver = context.contentResolver
        resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Downloads._ID),
            "${MediaStore.Downloads.DISPLAY_NAME} = ?",
            arrayOf(fileName),
            null,
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                resolver.delete(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build(),
                    null,
                    null,
                )
            }
        }

        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("No se ha podido crear el fichero en Descargas.")

        resolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write(csv)
        } ?: error("No se ha podido escribir el CSV final.")

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return CsvWriteResult(
            displayLocation = "Descargas/$fileName",
            uriString = uri.toString(),
        )
    }

    private fun writeToAppDownloads(
        fileName: String,
        csv: String,
    ): CsvWriteResult {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: error("No se encontró la carpeta de descargas de la app.")
        val file = File(downloadsDir, fileName)
        file.writeText(csv, Charsets.UTF_8)
        return CsvWriteResult(
            displayLocation = file.absolutePath,
        )
    }

    private fun buildCsv(rows: List<CentroDriveTimeRow>): String = buildString {
        appendCsvLine(
            listOf(
                "codigo",
                "denominacion",
                "regimen",
                "direccion",
                "longitud",
                "latitud",
                "provincia",
                "localidad",
                "distancia_km",
                "tiempo_minutos_coche",
            ),
        )
        rows.forEach { row ->
            appendCsvLine(
                listOf(
                    row.codigo,
                    row.denominacion,
                    row.regimen,
                    row.direccion,
                    row.longitud.toString(),
                    row.latitud.toString(),
                    row.provincia,
                    row.localidad,
                    row.distanciaKm.toString(),
                    row.tiempoMinutosCoche?.toString().orEmpty(),
                ),
            )
        }
    }

    private fun StringBuilder.appendCsvLine(values: List<String>) {
        append(values.joinToString(",") { value ->
            "\"${value.replace("\"", "\"\"")}\""
        })
        append('\n')
    }
}
