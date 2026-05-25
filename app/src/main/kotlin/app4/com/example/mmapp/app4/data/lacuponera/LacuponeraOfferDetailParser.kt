package com.example.mmapp.app4.data.lacuponera

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LacuponeraOfferDetailParser(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun parsePreferredStores(html: String): List<String> {
        val nextData = NEXT_DATA_REGEX.find(html)?.groupValues?.get(1)?.trim()
            ?: return emptyList()
        val root = runCatching { json.parseToJsonElement(nextData).jsonObject }.getOrNull()
            ?: return emptyList()

        val pageProps = root["props"]?.jsonObject
            ?.get("pageProps")?.jsonObject
            ?: return emptyList()

        val campaignId = pageProps["campaign"]?.jsonObject
            ?.get("id")?.jsonPrimitive?.intOrNull
            ?: return emptyList()

        val campaign = pageProps["campaigns"]?.jsonArray
            ?.firstOrNull { element ->
                element.jsonObject["id"]?.jsonPrimitive?.intOrNull == campaignId
            }?.jsonObject ?: return emptyList()

        val retailers = campaign["retailers"] as? JsonArray ?: return emptyList()
        return retailers.mapNotNull { retailer ->
            retailer.jsonObject["name"]?.jsonPrimitive?.contentOrNull()?.toPreferredStoreName()
        }.distinct()
            .sortedBy { PREFERRED_STORE_ORDER.indexOf(it).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
    }

    private fun String.toPreferredStoreName(): String? {
        val normalized = uppercase()
        return when {
            normalized.contains("MERCADONA") -> "Mercadona"
            normalized.contains("CARREFOUR") -> "Carrefour"
            normalized.contains("ALCAMPO") -> "Alcampo"
            normalized.contains("CONSUM") -> "Consum"
            normalized.contains("LIDL") -> "Lidl"
            else -> null
        }
    }

    private fun kotlinx.serialization.json.JsonPrimitive.contentOrNull(): String? = runCatching { content }.getOrNull()

    companion object {
        private val NEXT_DATA_REGEX = Regex(
            pattern = """<script id="__NEXT_DATA__" type="application/json"[^>]*>(.*?)</script>""",
            options = setOf(RegexOption.DOT_MATCHES_ALL),
        )

        private val PREFERRED_STORE_ORDER = listOf(
            "Mercadona",
            "Carrefour",
            "Alcampo",
            "Consum",
            "Lidl",
        )
    }
}
