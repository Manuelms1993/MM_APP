package com.example.mmapp.app4

import com.example.mmapp.app4.data.lacuponera.LacuponeraFreeOffersParser
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LacuponeraFreeOffersParserTest {
    private val parser = LacuponeraFreeOffersParser()

    @Test
    fun `extracts only free product offers from html`() {
        val html = """
            <html>
              <body>
                <a href="/promociones/ofertas/marca/1">Hasta 31/08 Fairy Lavavajillas Ultra Aplica a 20 lavados GRATIS</a>
                <a href="/promociones/ofertas/marca/2">Hasta 30/09 Fairy Spray Aplica a cualquier producto AHORRA 50%</a>
                <a href="/promociones/ofertas/marca/3">Hasta 30/09 Venus + Pantene Aplica a maquinillas venus + ampollas pantene Ampollas Gratis</a>
                <a href="/promociones/ofertas/marca/4">Hasta 31/08 GANA PRODUCTOS GRATIS Aplica a tickets PARTICIPA</a>
              </body>
            </html>
        """.trimIndent()

        val result = parser.parse(html, "https://www.lacuponera.es")

        assertThat(result.map { it.title }).containsExactly(
            "Hasta 31/08 Fairy Lavavajillas Ultra Aplica a 20 lavados GRATIS",
            "Hasta 30/09 Venus + Pantene Aplica a maquinillas venus + ampollas pantene Ampollas Gratis",
        )
        assertThat(result.map { it.url }).containsExactly(
            "https://www.lacuponera.es/promociones/ofertas/marca/1",
            "https://www.lacuponera.es/promociones/ofertas/marca/3",
        )
    }
}
