package com.example.plantreminder

import com.example.plantreminder.data.input.FlexibleTaskParser
import com.example.plantreminder.data.input.RawPlantInput
import com.example.plantreminder.data.input.validation.PlantCatalogValidator
import com.example.plantreminder.data.input.validation.PlantDefinitionValidator
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

    private fun loadRepositoryInputs(): List<RawPlantInput> {
        val inputDirectory = sequenceOf(File("inputs"), File("../inputs"))
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
