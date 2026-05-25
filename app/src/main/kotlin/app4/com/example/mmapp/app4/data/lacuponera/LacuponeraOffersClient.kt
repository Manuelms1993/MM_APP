package com.example.mmapp.app4.data.lacuponera

import java.net.HttpURLConnection
import java.net.URL

class LacuponeraOffersClient(
    private val parser: LacuponeraFreeOffersParser,
    private val detailParser: LacuponeraOfferDetailParser,
) {
    fun findFreeOffers(
        rawUrl: String,
    ): LacuponeraFetchResult {
        val normalizedUrl = normalizeUrl(rawUrl)
        val connection = URL(normalizedUrl).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Android) MM_APP Script Runner",
        )
        connection.setRequestProperty("Accept", "text/html")

        return try {
            val statusCode = connection.responseCode
            require(statusCode in 200..299) { "La Cuponera devolvió HTTP $statusCode." }
            val html = connection.inputStream.bufferedReader().use { it.readText() }
            val freeOffers = parser.parse(html, baseUrl = "https://www.lacuponera.es")
                .map { offer ->
                    val offerHtml = fetchHtml(offer.url)
                    offer.copy(preferredStores = detailParser.parsePreferredStores(offerHtml))
                }
            LacuponeraFetchResult(
                requestedUrl = normalizedUrl,
                resolvedUrl = connection.url.toString(),
                freeOffers = freeOffers,
                htmlSizeBytes = html.toByteArray().size,
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun normalizeUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        val noQuery = trimmed.substringBefore("?")
        return noQuery.removeSuffix("/")
    }

    private fun fetchHtml(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = true
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Android) MM_APP Script Runner",
        )
        connection.setRequestProperty("Accept", "text/html")
        return try {
            val statusCode = connection.responseCode
            require(statusCode in 200..299) { "La Cuponera devolvió HTTP $statusCode al abrir $url." }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
