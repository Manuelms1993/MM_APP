package com.example.mmapp.app2

import com.example.mmapp.app2.data.input.MenuPlanParser
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MenuPlanParserTest {
    private val parser = MenuPlanParser()

    @Test
    fun `parsea un plan valido con semanas y opciones`() {
        val json = """
            {
              "schemaVersion": 1,
              "planType": "comidas",
              "fechaInicio": "2026-05-04",
              "semanas": [
                {
                  "numeroSemana": 1,
                  "dias": [
                    {"diaSemana": "lunes", "opciones": [{"nombre": "Arroz", "ingredientes": ["arroz", "pollo"]}]},
                    {"diaSemana": "martes", "opciones": [{"nombre": "Pasta", "ingredientes": ["pasta"]}]},
                    {"diaSemana": "miercoles", "opciones": [{"nombre": "Lentejas", "ingredientes": ["lentejas"]}]},
                    {"diaSemana": "jueves", "opciones": [{"nombre": "Crema", "ingredientes": ["calabacin"]}]},
                    {"diaSemana": "viernes", "opciones": [{"nombre": "Burrito", "ingredientes": ["tortilla"]}]},
                    {"diaSemana": "sabado", "opciones": [{"nombre": "Paella", "ingredientes": ["arroz"]}]},
                    {"diaSemana": "domingo", "opciones": [{"nombre": "Macarrones", "ingredientes": ["pasta"]}]}
                  ]
                },
                {
                  "numeroSemana": 2,
                  "dias": [
                    {"diaSemana": "lunes", "opciones": [{"nombre": "A", "ingredientes": ["a"]}]},
                    {"diaSemana": "martes", "opciones": [{"nombre": "B", "ingredientes": ["b"]}]},
                    {"diaSemana": "miercoles", "opciones": [{"nombre": "C", "ingredientes": ["c"]}]},
                    {"diaSemana": "jueves", "opciones": [{"nombre": "D", "ingredientes": ["d"]}]},
                    {"diaSemana": "viernes", "opciones": [{"nombre": "E", "ingredientes": ["e"]}]},
                    {"diaSemana": "sabado", "opciones": [{"nombre": "F", "ingredientes": ["f"]}]},
                    {"diaSemana": "domingo", "opciones": [{"nombre": "G", "ingredientes": ["g"]}]}
                  ]
                },
                {
                  "numeroSemana": 3,
                  "dias": [
                    {"diaSemana": "lunes", "opciones": [{"nombre": "A", "ingredientes": ["a"]}]},
                    {"diaSemana": "martes", "opciones": [{"nombre": "B", "ingredientes": ["b"]}]},
                    {"diaSemana": "miercoles", "opciones": [{"nombre": "C", "ingredientes": ["c"]}]},
                    {"diaSemana": "jueves", "opciones": [{"nombre": "D", "ingredientes": ["d"]}]},
                    {"diaSemana": "viernes", "opciones": [{"nombre": "E", "ingredientes": ["e"]}]},
                    {"diaSemana": "sabado", "opciones": [{"nombre": "F", "ingredientes": ["f"]}]},
                    {"diaSemana": "domingo", "opciones": [{"nombre": "G", "ingredientes": ["g"]}]}
                  ]
                },
                {
                  "numeroSemana": 4,
                  "dias": [
                    {"diaSemana": "lunes", "opciones": [{"nombre": "A", "ingredientes": ["a"]}]},
                    {"diaSemana": "martes", "opciones": [{"nombre": "B", "ingredientes": ["b"]}]},
                    {"diaSemana": "miercoles", "opciones": [{"nombre": "C", "ingredientes": ["c"]}]},
                    {"diaSemana": "jueves", "opciones": [{"nombre": "D", "ingredientes": ["d"]}]},
                    {"diaSemana": "viernes", "opciones": [{"nombre": "E", "ingredientes": ["e"]}]},
                    {"diaSemana": "sabado", "opciones": [{"nombre": "F", "ingredientes": ["f"]}]},
                    {"diaSemana": "domingo", "opciones": [{"nombre": "G", "ingredientes": ["g"]}]}
                  ]
                }
              ]
            }
        """.trimIndent()

        val result = parser.create(json, "comidas.json")

        assertThat(result.warnings).isEmpty()
        assertThat(result.menuPlan).isNotNull()
        assertThat(result.menuPlan!!.weeks).hasSize(4)
        assertThat(result.menuPlan!!.weeks.first().days.first().options.first().name).isEqualTo("Arroz")
    }
}
