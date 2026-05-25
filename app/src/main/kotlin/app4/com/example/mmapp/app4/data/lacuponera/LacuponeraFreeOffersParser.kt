package com.example.mmapp.app4.data.lacuponera

class LacuponeraFreeOffersParser {
    fun parse(html: String, baseUrl: String): List<FreeOffer> {
        val anchorRegex = Regex(
            pattern = """<a\b[^>]*href="([^"]+)"[^>]*>(.*?)</a>""",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )

        return anchorRegex.findAll(html)
            .mapNotNull { match ->
                val rawHref = match.groupValues[1].trim()
                val text = match.groupValues[2]
                    .stripHtmlTags()
                    .decodeHtmlEntities()
                    .normalizeWhitespace()
                if (!isCandidateOffer(rawHref, text)) return@mapNotNull null
                FreeOffer(
                    title = text,
                    url = rawHref.toAbsoluteUrl(baseUrl),
                )
            }
            .distinctBy { it.url }
            .sortedBy { it.title.lowercase() }
            .toList()
    }

    private fun isCandidateOffer(href: String, text: String): Boolean {
        if (!href.contains("/promociones/ofertas", ignoreCase = true)) return false
        if (!text.contains("gratis", ignoreCase = true)) return false
        if (!text.contains("aplica", ignoreCase = true)) return false
        if (text.contains("participa", ignoreCase = true)) return false
        return true
    }

    private fun String.toAbsoluteUrl(baseUrl: String): String = when {
        startsWith("http://") || startsWith("https://") -> this
        startsWith("/") -> "$baseUrl$this"
        else -> "$baseUrl/$this"
    }

    private fun String.stripHtmlTags(): String = replace(Regex("<[^>]+>"), " ")

    private fun String.decodeHtmlEntities(): String = this
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&#39;", "'")
        .replace("&quot;", "\"")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

    private fun String.normalizeWhitespace(): String = replace(Regex("\\s+"), " ").trim()
}
