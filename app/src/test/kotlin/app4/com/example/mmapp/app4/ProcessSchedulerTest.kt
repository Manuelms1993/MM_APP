package com.example.mmapp.app4

import com.example.mmapp.app4.work.ProcessScheduler
import com.google.common.truth.Truth.assertThat
import java.time.Duration
import java.time.ZonedDateTime
import org.junit.Test

class ProcessSchedulerTest {
    @Test
    fun `si la hora ya paso respeta el intervalo configurado para el primer disparo`() {
        val now = ZonedDateTime.of(2026, 5, 13, 10, 30, 0, 0, ProcessScheduler.MADRID_ZONE)

        val delay = ProcessScheduler.initialDelay(hourOfDay = 9, intervalDays = 7, now = now)

        assertThat(delay).isEqualTo(Duration.ofDays(6).plusHours(22).plusMinutes(30))
    }

    @Test
    fun `si la hora aun no paso programa hoy`() {
        val now = ZonedDateTime.of(2026, 5, 13, 8, 30, 0, 0, ProcessScheduler.MADRID_ZONE)

        val delay = ProcessScheduler.initialDelay(hourOfDay = 9, intervalDays = 7, now = now)

        assertThat(delay).isEqualTo(Duration.ofMinutes(30))
    }
}
