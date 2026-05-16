package com.example.plantreminder.data.input.validation

import com.example.plantreminder.domain.models.PlantDefinition

class PlantDefinitionValidator {
    fun validate(plant: PlantDefinition, sourceName: String): List<String> = buildList {
        if (plant.riego == null) {
            add("Falta la regla de riego en $sourceName.")
        }
        if (plant.riego != null && plant.riego.frequenciesBySeason.frequenciesBySeason.isEmpty()) {
            add("No hay frecuencias de riego válidas en $sourceName.")
        }
        if (plant.mostrarEnSiembra && plant.mesesSiembra.isEmpty()) {
            add("mostrarEnSiembra=true pero no hay mesesSiembra en $sourceName.")
        }
        if (plant.fuenteInformacionUrl.isNullOrBlank()) {
            add("Falta fuenteInformacionUrl en $sourceName.")
        }
        if (plant.fuenteSustratoUrl.isNullOrBlank()) {
            add("Falta fuenteSustratoUrl en $sourceName.")
        }
        if (plant.metadata["tamanoMaceta"].isNullOrBlank()) {
            add("Falta metadata.tamanoMaceta en $sourceName.")
        }
        if (plant.metadata["prioridad"].isNullOrBlank()) {
            add("Falta metadata.prioridad en $sourceName.")
        }
    }
}
