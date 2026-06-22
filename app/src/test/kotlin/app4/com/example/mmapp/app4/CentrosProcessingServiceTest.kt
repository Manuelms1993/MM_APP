package com.example.mmapp.app4

import com.example.mmapp.app4.data.centros.CentroCsvRow
import com.example.mmapp.app4.data.centros.CentrosCsvAssetDataSource
import com.example.mmapp.app4.data.centros.CentrosProcessingService
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CentrosProcessingServiceTest {
    private val service = CentrosProcessingService()

    @Test
    fun `filtra solo institutos publicos`() {
        val rows = listOf(
            CentroCsvRow("1", "INSTITUTO DE EDUCACIÓN SECUNDARIA", "IES Uno", "PÚB.", "A", -0.1, 39.0, "VALENCIA/VALÈNCIA", "VALENCIA"),
            CentroCsvRow("2", "COLEGIO DE EDUCACIÓN INFANTIL Y PRIMARIA", "CEIP Dos", "PÚB.", "B", -0.2, 39.1, "VALENCIA/VALÈNCIA", "VALENCIA"),
            CentroCsvRow("3", "INSTITUTO DE EDUCACIÓN SECUNDARIA", "IES Tres", "PRIV. CONC.", "C", -0.3, 39.2, "VALENCIA/VALÈNCIA", "VALENCIA"),
        )

        val result = service.filterEligibleSchools(rows)

        assertThat(result.map { it.codigo }).containsExactly("1")
    }

    @Test
    fun `normaliza el nombre final del csv`() {
        assertThat(service.normalizeOutputFileName(" centros final ")).isEqualTo("centros_final.csv")
        assertThat(service.normalizeOutputFileName("salida.csv")).isEqualTo("salida.csv")
        assertThat(service.normalizeOutputFileName("a:b/c")).isEqualTo("a_b_c.csv")
    }

    @Test
    fun `parsea csv con comillas y separador punto y coma`() {
        val csv = "\"codigo\";\"denominacion\";longitud;latitud\n" +
            "\"1\";\"IES \"\"Demo\"\"\";-0.1;39.0"

        val records = CentrosCsvAssetDataSource.parseDelimitedCsv(csv, ';')

        assertThat(records).hasSize(2)
        assertThat(records[1][1]).isEqualTo("IES \"Demo\"")
    }
}
