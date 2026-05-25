package com.example.mmapp.app4.data.lacuponera

data class FreeOffer(
    val title: String,
    val url: String,
    val preferredStores: List<String> = emptyList(),
)

data class LacuponeraFetchResult(
    val requestedUrl: String,
    val resolvedUrl: String,
    val freeOffers: List<FreeOffer>,
    val htmlSizeBytes: Int,
)
