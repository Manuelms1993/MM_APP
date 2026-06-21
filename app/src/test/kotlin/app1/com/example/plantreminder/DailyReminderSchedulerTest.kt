package com.example.mmapp.app1

import com.example.mmapp.app1.work.DailyReminderScheduler
import com.google.common.truth.Truth.assertThat
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Test

class DailyReminderSchedulerTest {
    @Test
    fun `programa el recordatorio de plantas en horario de madrid`() {
        val now = ZonedDateTime.of(2026, 5, 13, 6, 30, 0, 0, ZoneId.of("UTC"))

        val next = DailyReminderScheduler.nextTriggerAt(now, LocalTime.of(9, 0), intervalDays = 1)

        assertThat(next).isEqualTo(
            ZonedDateTime.of(2026, 5, 13, 9, 0, 0, 0, DailyReminderScheduler.MADRID_ZONE),
        )
    }

    @Test
    fun `si la hora ya paso respeta el intervalo configurado`() {
        val now = ZonedDateTime.of(2026, 5, 13, 10, 30, 0, 0, DailyReminderScheduler.MADRID_ZONE)

        val next = DailyReminderScheduler.nextTriggerAt(now, LocalTime.of(9, 0), intervalDays = 3)

        assertThat(next).isEqualTo(
            ZonedDateTime.of(2026, 5, 16, 9, 0, 0, 0, DailyReminderScheduler.MADRID_ZONE),
        )
    }

    @Test
    fun `mantiene la hora de madrid tras el cambio a horario de invierno`() {
        val now = ZonedDateTime.of(2026, 10, 24, 22, 30, 0, 0, DailyReminderScheduler.MADRID_ZONE)

        val next = DailyReminderScheduler.nextTriggerAt(now, LocalTime.of(9, 0), intervalDays = 1)

        assertThat(next).isEqualTo(
            ZonedDateTime.of(2026, 10, 25, 9, 0, 0, 0, DailyReminderScheduler.MADRID_ZONE),
        )
        assertThat(next.offset.totalSeconds).isEqualTo(3600)
    }
}
