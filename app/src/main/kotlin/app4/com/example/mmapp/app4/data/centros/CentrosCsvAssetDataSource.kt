package com.example.mmapp.app4.data.centros

import android.content.Context

class CentrosCsvAssetDataSource(
    private val context: Context,
) {
    fun loadRows(): List<CentroCsvRow> {
        val csvText = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        val records = parseDelimitedCsv(csvText, separator = ';')
        require(records.isNotEmpty()) { "El fichero de centros está vacío." }

        val header = records.first().map { it.trim() }
        return records.drop(1).mapNotNull { row ->
            val valuesByName = header.zip(row).toMap()
            val longitud = valuesByName["longitud"]?.toDoubleOrNull() ?: return@mapNotNull null
            val latitud = valuesByName["latitud"]?.toDoubleOrNull() ?: return@mapNotNull null

            CentroCsvRow(
                codigo = valuesByName["codigo"].orEmpty(),
                denominacionGenericaEs = valuesByName["denominacion_generica_es"].orEmpty(),
                denominacion = valuesByName["denominacion"].orEmpty(),
                regimen = valuesByName["regimen"].orEmpty(),
                direccion = valuesByName["direccion"].orEmpty(),
                longitud = longitud,
                latitud = latitud,
                provincia = valuesByName["provincia"].orEmpty(),
                localidad = valuesByName["localidad"].orEmpty(),
            ).takeIf {
                it.codigo.isNotBlank() && it.denominacion.isNotBlank()
            }
        }
    }

    companion object {
        private const val ASSET_PATH = "processes/centros.csv"

        internal fun parseDelimitedCsv(
            input: String,
            separator: Char,
        ): List<List<String>> {
            val records = mutableListOf<List<String>>()
            val row = mutableListOf<String>()
            val cell = StringBuilder()
            var insideQuotes = false
            var index = 0

            while (index < input.length) {
                val char = input[index]
                when {
                    char == '"' -> {
                        if (insideQuotes && index + 1 < input.length && input[index + 1] == '"') {
                            cell.append('"')
                            index++
                        } else {
                            insideQuotes = !insideQuotes
                        }
                    }

                    char == separator && !insideQuotes -> {
                        row += cell.toString()
                        cell.clear()
                    }

                    (char == '\n' || char == '\r') && !insideQuotes -> {
                        if (char == '\r' && index + 1 < input.length && input[index + 1] == '\n') {
                            index++
                        }
                        row += cell.toString()
                        cell.clear()
                        if (row.any { it.isNotEmpty() }) {
                            records += row.toList()
                        }
                        row.clear()
                    }

                    else -> cell.append(char)
                }
                index++
            }

            if (cell.isNotEmpty() || row.isNotEmpty()) {
                row += cell.toString()
                if (row.any { it.isNotEmpty() }) {
                    records += row.toList()
                }
            }

            return records
        }
    }
}
