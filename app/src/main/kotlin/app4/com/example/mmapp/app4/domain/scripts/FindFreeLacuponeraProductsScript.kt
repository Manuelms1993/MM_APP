package com.example.mmapp.app4.domain.scripts

import com.example.mmapp.app4.data.lacuponera.LacuponeraOffersClient
import com.example.mmapp.app4.domain.models.ScriptDefinition
import com.example.mmapp.app4.domain.models.ScriptExecutionResult
import com.example.mmapp.app4.domain.models.ScriptLogEntry
import com.example.mmapp.app4.domain.models.ScriptLogLevel
import com.example.mmapp.app4.domain.models.ScriptResultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FindFreeLacuponeraProductsScript(
    private val client: LacuponeraOffersClient,
) : ScriptTask {
    override val definition: ScriptDefinition = ScriptDefinition(
        id = "lacuponera_free_products",
        topic = "La Cuponera",
        title = "Buscar productos gratis",
        description = "Revisa el listado público de promociones y detecta ofertas marcadas como gratis.",
    )

    override suspend fun execute(
        onLog: (ScriptLogEntry) -> Unit,
    ): ScriptExecutionResult = withContext(Dispatchers.IO) {
        val requestedUrl = "https://www.lacuponera.es/promociones/ofertas"
        val initialLogs = mutableListOf(
            ScriptLogEntry(
                message = "Ejecutando proceso: ${definition.title}",
                level = ScriptLogLevel.INFO,
            ),
            ScriptLogEntry(
                message = "URL normalizada: $requestedUrl",
                level = ScriptLogLevel.INFO,
            ),
        )
        initialLogs.forEach(onLog)

        runCatching { client.findFreeOffers(requestedUrl) }
            .fold(
                onSuccess = { result ->
                    val items = result.freeOffers.map { offer ->
                        ScriptResultItem(
                            title = offer.title,
                            detail = if (offer.preferredStores.isEmpty()) {
                                "Tiendas objetivo: no encontrada ninguna de Mercadona, Carrefour, Alcampo, Consum o Lidl"
                            } else {
                                "Tiendas objetivo: ${offer.preferredStores.joinToString(", ")}"
                            },
                            linkUrl = offer.url,
                        )
                    }
                    ScriptExecutionResult(
                        summary = if (items.isEmpty()) {
                            "No se han encontrado ofertas gratis."
                        } else {
                            "Encontradas ${items.size} ofertas gratis."
                        },
                        logs = initialLogs + listOf(
                            ScriptLogEntry(
                                message = "HTML descargado: ${result.htmlSizeBytes} bytes",
                                level = ScriptLogLevel.INFO,
                            ),
                            ScriptLogEntry(
                                message = "URL resuelta: ${result.resolvedUrl}",
                                level = ScriptLogLevel.INFO,
                            ),
                            ScriptLogEntry(
                                message = "Proceso OK",
                                level = ScriptLogLevel.SUCCESS,
                            ),
                            ScriptLogEntry(
                                message = "Resultados: ${items.size} oferta(s) gratis detectada(s)",
                                level = ScriptLogLevel.SUCCESS,
                            ),
                        ),
                        items = items,
                    )
                },
                onFailure = { throwable ->
                    ScriptExecutionResult(
                        summary = "El script ha fallado.",
                        logs = initialLogs + listOf(
                            ScriptLogEntry(
                                message = "Error: ${throwable.message ?: "Fallo desconocido"}",
                                level = ScriptLogLevel.ERROR,
                            ),
                        ),
                        items = emptyList(),
                    )
                },
            )
    }
}
