package com.example.mmapp.app1

import com.example.mmapp.app1.domain.MessageBuilder
import com.example.mmapp.app1.domain.models.DailyPlantAction
import com.example.mmapp.app1.domain.models.PlantActionType
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class MessageBuilderTest {
    private val builder = MessageBuilder()

    @Test
    fun buildsNaturalMessageWithGroupedPlants() {
        val date = LocalDate.parse("2026-05-06")
        val actions = listOf(
            DailyPlantAction(date, "romero", "Romero", PlantActionType.WATER, "regar", listOf("Evitar exceso de agua"), "{}"),
            DailyPlantAction(date, "romero", "Romero", PlantActionType.FERTILIZE, "aplicar humus de lombriz", emptyList(), "{}"),
            DailyPlantAction(date, "albahaca", "Albahaca", PlantActionType.WATER, "regar", listOf("Mantener sustrato ligeramente húmedo"), "{}"),
        )

        val result = builder.build(date, actions)

        assertThat(result.text).doesNotContain("Hoy toca cuidar tus plantas:")
        assertThat(result.text).contains("• Albahaca")
        assertThat(result.text).contains("  - Regar")
        assertThat(result.text).contains("• Romero")
        assertThat(result.text).contains("  - Abono")
        assertThat(result.text).contains("    · Aplicar humus de lombriz")
        assertThat(result.text.indexOf("• Albahaca")).isLessThan(result.text.indexOf("• Romero"))
    }

    @Test
    fun buildsNoActionsMessage() {
        val result = builder.build(LocalDate.parse("2026-05-06"), emptyList())

        assertThat(result.text).isEqualTo("Sin acciones para este día.")
    }
}
