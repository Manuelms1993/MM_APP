package com.example.mmapp.app4

import com.example.mmapp.app4.data.lacuponera.LacuponeraOfferDetailParser
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LacuponeraOfferDetailParserTest {
    private val parser = LacuponeraOfferDetailParser()

    @Test
    fun `extracts preferred stores from next data`() {
        val html = """
            <html>
              <body>
                <script id="__NEXT_DATA__" type="application/json">
                  {
                    "props": {
                      "pageProps": {
                        "campaign": {
                          "id": 1440
                        },
                        "campaigns": [
                          {
                            "id": 1440,
                            "retailers": [
                              { "name": "MERCADONA" },
                              { "name": "CARREFOUR HIPERMERCADO" },
                              { "name": "ALCAMPO" },
                              { "name": "CONSUM" },
                              { "name": "LIDL" },
                              { "name": "AMAZON" }
                            ]
                          }
                        ]
                      }
                    }
                  }
                </script>
              </body>
            </html>
        """.trimIndent()

        val result = parser.parsePreferredStores(html)

        assertThat(result).containsExactly(
            "Mercadona",
            "Carrefour",
            "Alcampo",
            "Consum",
            "Lidl",
        ).inOrder()
    }
}
