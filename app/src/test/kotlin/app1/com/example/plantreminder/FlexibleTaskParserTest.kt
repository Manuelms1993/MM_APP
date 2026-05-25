package com.example.mmapp.app1

import com.example.mmapp.app1.data.input.FlexibleTaskParser
import com.example.mmapp.app1.domain.models.FertilizerDoseMode
import com.example.mmapp.app1.domain.models.Season
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FlexibleTaskParserTest {
    private val parser = FlexibleTaskParser()

    @Test
    fun parserToleratesExtraFields() {
        val rawJson = """
            {
              "schemaVersion": 1,
              "futureField": "ignored",
              "planta": {
                "id": "romero-001",
                "nombre": "Romero",
                "fechaInicio": "2026-05-06",
                "riego": {
                  "cadaDias": {
                    "primavera": 5
                  },
                  "notas": ["Evitar exceso de agua"]
                },
                "abono": {
                  "primavera": [
                    {
                      "tipo": "humus_lombriz",
                      "cadaDias": 45,
                      "extraRule": true
                    }
                  ]
                }
              }
            }
        """.trimIndent()

        val result = parser.parse(rawJson, "romero.json")

        assertThat(result.plantDefinition).isNotNull()
        assertThat(result.plantDefinition!!.nombre).isEqualTo("Romero")
        assertThat(result.warnings).isEmpty()
    }

    @Test
    fun parserMapsSowingVisibilityAndSourceUrls() {
        val rawJson = """
            {
              "schemaVersion": 1,
              "planta": {
                "id": "perejil-001",
                "nombre": "Perejil",
                "fechaInicio": "2026-05-06",
                "responsable": "R",
                "mostrarEnSiembra": true,
                "fuenteInformacionUrl": "https://example.com/info",
                "fuenteSustratoUrl": "https://example.com/sustrato"
              }
            }
        """.trimIndent()

        val result = parser.parse(rawJson, "perejil.json")
        val plant = result.plantDefinition!!

        assertThat(plant.responsable).isEqualTo("R")
        assertThat(plant.mostrarEnSiembra).isTrue()
        assertThat(plant.fuenteInformacionUrl).isEqualTo("https://example.com/info")
        assertThat(plant.fuenteSustratoUrl).isEqualTo("https://example.com/sustrato")
    }

    @Test
    fun parserDefaultsActivatedToTrueAndAcceptsFalse() {
        val enabledRawJson = """
            {
              "schemaVersion": 1,
              "planta": {
                "id": "rucula-001",
                "nombre": "Rucula",
                "fechaInicio": "2026-05-15"
              }
            }
        """.trimIndent()
        val disabledRawJson = """
            {
              "schemaVersion": 1,
              "planta": {
                "id": "rabano-001",
                "nombre": "Rabano",
                "fechaInicio": "2026-05-15",
                "activada": false
              }
            }
        """.trimIndent()

        val enabledPlant = parser.parse(enabledRawJson, "rucula.json").plantDefinition!!
        val disabledPlant = parser.parse(disabledRawJson, "rabano.json").plantDefinition!!

        assertThat(enabledPlant.activada).isTrue()
        assertThat(disabledPlant.activada).isFalse()
    }

    @Test
    fun parserMapsHalfDoseMode() {
        val rawJson = """
            {
              "schemaVersion": 1,
              "planta": {
                "id": "romero-001",
                "nombre": "Romero",
                "fechaInicio": "2026-05-06",
                "abono": {
                  "primavera": [
                    {
                      "tipo": "fertilizante_liquido_mediterraneas",
                      "cadaDias": 30,
                      "dosis": "fabricante_dividido_2"
                    }
                  ]
                }
              }
            }
        """.trimIndent()

        val result = parser.parse(rawJson, "romero.json")
        val plant = result.plantDefinition!!

        assertThat(plant.abono[Season.PRIMAVERA]!!.first().dosis)
            .isEqualTo(FertilizerDoseMode.FABRICANTE_DIVIDIDO_2)
    }

    @Test
    fun parserMapsPottingMixComposition() {
        val rawJson = """
            {
              "schemaVersion": 1,
              "planta": {
                "id": "olivo-001",
                "nombre": "Olivo",
                "fechaInicio": "2026-05-06",
                "composicionMaceta": {
                  "componentes": [
                    { "material": "sustrato_universal", "porcentaje": 60 },
                    { "material": "perlita", "porcentaje": 20 },
                    { "material": "grava_volcanica", "porcentaje": 20 }
                  ],
                  "notas": ["Mezcla drenante"]
                }
              }
            }
        """.trimIndent()

        val result = parser.parse(rawJson, "olivo.json")
        val plant = result.plantDefinition!!
        val pottingMix = plant.composicionMaceta!!

        assertThat(pottingMix).isNotNull()
        assertThat(pottingMix.components).hasSize(3)
        assertThat(pottingMix.components.sumOf { it.percentage }).isEqualTo(100)
        assertThat(pottingMix.notes).containsExactly("Mezcla drenante")
    }
}
