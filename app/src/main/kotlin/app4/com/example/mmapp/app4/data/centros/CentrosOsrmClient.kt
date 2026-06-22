package com.example.mmapp.app4.data.centros

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CentrosOsrmClient(
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun fetchDriveTimeMinutes(
        originLatitude: Double,
        originLongitude: Double,
        destinationLatitude: Double,
        destinationLongitude: Double,
    ): Double? {
        val requestUrl = buildString {
            append("https://router.project-osrm.org/route/v1/driving/")
            append("$originLongitude,$originLatitude;$destinationLongitude,$destinationLatitude")
            append("?overview=false")
        }

        val connection = URL(requestUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Accept", "application/json")

        return try {
            if (connection.responseCode !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = json.parseToJsonElement(body).jsonObject
            if (root["code"]?.jsonPrimitive?.content != "Ok") return null
            val durationSeconds = root["routes"]?.jsonArray
                ?.firstOrNull()
                ?.jsonObject
                ?.get("duration")
                ?.jsonPrimitive
                ?.doubleOrNull
                ?: return null
            durationSeconds / 60.0
        } finally {
            connection.disconnect()
        }
    }
}
