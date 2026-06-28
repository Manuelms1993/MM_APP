package com.example.mmapp.app1

import com.example.mmapp.app1.data.input.FlexibleTaskParser
import com.example.mmapp.app1.data.input.RawPlantInput
import com.example.mmapp.app1.data.input.validation.PlantCatalogValidator
import com.example.mmapp.app1.data.input.validation.PlantDefinitionValidator
import com.example.mmapp.app1.domain.models.Season
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class PlantCatalogValidatorTest {
    private val validator = PlantCatalogValidator(
        plantDefinitionFactory = FlexibleTaskParser(),
        plantDefinitionValidator = PlantDefinitionValidator(),
    )

    @Test
    fun validatesAllRepositoryInputsWithoutWarnings() {
        val inputs = loadRepositoryInputs()

        val report = validator.validate(inputs)

        assertThat(report.plants).hasSize(inputs.size)
        assertThat(report.warnings).isEmpty()
    }

    @Test
    fun reportsDuplicateIds() {
        val duplicatedInput = RawPlantInput(
            fileName = "duplicado.json",
            rawJson = """
                {
                  "planta": {
                    "id": "albahaca-001",
                    "nombre": "Albahaca Duplicada",
                    "fechaInicio": "2026-05-01",
                    "riego": {
                      "cadaDias": { "primavera": 2 }
                    },
                    "fuenteInformacionUrl": "https://example.com/info",
                    "fuenteSustratoUrl": "https://example.com/sustrato",
                    "metadata": {
                      "tamanoMaceta": "mediana",
                      "prioridad": "alta"
                    }
                  }
                }
            """.trimIndent(),
        )

        val report = validator.validate(loadRepositoryInputs() + duplicatedInput)

        assertThat(report.warnings.joinToString("\n")).contains("ID duplicado en inputs: albahaca-001")
    }

    @Test
    fun repositoryInputsKeepConservativeIndoorWateringRules() {
        val plants = validator.validate(loadRepositoryInputs()).plants.associateBy { it.id }

        assertThat(plants["bonsai-ficus-pequeno-001"]!!.riego!!.frequenciesBySeason.forSeason(Season.PRIMAVERA))
            .isAtLeast(7)
        assertThat(plants["ficus-microcarpa-001"]!!.riego!!.frequenciesBySeason.forSeason(Season.VERANO))
            .isAtLeast(10)
        assertThat(plants["filodendro-001"]!!.riego!!.frequenciesBySeason.forSeason(Season.VERANO))
            .isAtLeast(7)
        assertThat(plants["lengua-suegra-001"]!!.riego!!.frequenciesBySeason.forSeason(Season.PRIMAVERA))
            .isAtLeast(21)
        assertThat(plants["lengua-suegra-001"]!!.riego!!.frequenciesBySeason.forSeason(Season.INVIERNO))
            .isAtLeast(35)

        val criticalIndoorNotes = listOf(
            plants["bonsai-ficus-pequeno-001"]!!,
            plants["ficus-microcarpa-001"]!!,
            plants["filodendro-001"]!!,
            plants["lengua-suegra-001"]!!,
        ).flatMap { it.riego!!.notes + it.notas }.joinToString("\n").lowercase()

        assertThat(criticalIndoorNotes).contains("comprobar")
        assertThat(criticalIndoorNotes).contains("pausar riego")
    }

    @Test
    fun repositoryInputsReduceIndoorFertilizerWhenHumidityStressIsLikely() {
        val plants = validator.validate(loadRepositoryInputs()).plants.associateBy { it.id }

        listOf(
            "bonsai-ficus-pequeno-001",
            "ficus-microcarpa-001",
            "filodendro-001",
        ).forEach { plantId ->
            val plant = plants[plantId]!!
            assertThat(plant.abono[Season.PRIMAVERA].orEmpty().single().cadaDias).isAtLeast(45)
            assertThat(plant.abono[Season.VERANO].orEmpty().single().cadaDias).isAtLeast(45)
            assertThat(plant.abono[Season.OTONO].orEmpty()).isEmpty()
            assertThat(plant.abono[Season.INVIERNO].orEmpty()).isEmpty()
        }

        val snakePlant = plants["lengua-suegra-001"]!!
        assertThat(snakePlant.abono[Season.PRIMAVERA].orEmpty().single().cadaDias).isAtLeast(60)
        assertThat(snakePlant.abono[Season.VERANO].orEmpty()).isEmpty()
        assertThat(snakePlant.abono[Season.OTONO].orEmpty()).isEmpty()
        assertThat(snakePlant.abono[Season.INVIERNO].orEmpty()).isEmpty()
    }

    private fun loadRepositoryInputs(): List<RawPlantInput> {
        val inputDirectory = sequenceOf(
            File("inputs/plants"),
            File("../inputs/plants"),
            File("inputs"),
            File("../inputs"),
        )
            .firstOrNull { candidate ->
                candidate.exists() && candidate.isDirectory && candidate.listFiles().orEmpty().any { it.extension == "json" }
            }
            ?: error("No se encontró el directorio inputs para los tests.")

        return inputDirectory.listFiles()
            .orEmpty()
            .filter { it.isFile && it.extension == "json" }
            .sortedBy { it.name }
            .map { file ->
                RawPlantInput(
                    fileName = file.name,
                    rawJson = file.readText(),
                )
            }
    }
}
