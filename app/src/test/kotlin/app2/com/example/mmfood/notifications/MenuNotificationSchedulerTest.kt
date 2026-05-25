package com.example.mmapp.app2.notifications

import com.google.common.truth.Truth.assertThat
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Test

class MenuNotificationSchedulerTest {
    @Test
    fun `calcula la siguiente comida en horario de madrid aunque el instante venga en otra zona`() {
        val now = ZonedDateTime.of(2026, 5, 13, 7, 30, 0, 0, ZoneId.of("UTC"))

        val next = MenuNotificationScheduler.nextTriggerAt(now, LocalTime.of(10, 0), intervalDays = 1)

        assertThat(next).isEqualTo(
            ZonedDateTime.of(2026, 5, 13, 10, 0, 0, 0, MenuNotificationScheduler.MADRID_ZONE_ID),
        )
    }

    @Test
    fun `si la hora de cena ya paso programa el dia siguiente en madrid`() {
        val now = ZonedDateTime.of(2026, 5, 13, 18, 30, 0, 0, MenuNotificationScheduler.MADRID_ZONE_ID)

        val next = MenuNotificationScheduler.nextTriggerAt(now, LocalTime.of(18, 0), intervalDays = 1)

        assertThat(next).isEqualTo(
            ZonedDateTime.of(2026, 5, 14, 18, 0, 0, 0, MenuNotificationScheduler.MADRID_ZONE_ID),
        )
    }

    @Test
    fun `mantiene las 10 de madrid tras el cambio a horario de invierno`() {
        val now = ZonedDateTime.of(2026, 10, 24, 22, 30, 0, 0, MenuNotificationScheduler.MADRID_ZONE_ID)

        val next = MenuNotificationScheduler.nextTriggerAt(now, LocalTime.of(10, 0), intervalDays = 1)

        assertThat(next).isEqualTo(
            ZonedDateTime.of(2026, 10, 25, 10, 0, 0, 0, MenuNotificationScheduler.MADRID_ZONE_ID),
        )
        assertThat(next.offset.totalSeconds).isEqualTo(3600)
    }
}
