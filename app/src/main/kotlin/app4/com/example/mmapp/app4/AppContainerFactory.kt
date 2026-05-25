package com.example.mmapp.app4

import android.app.Application
import com.example.mmapp.app4.data.lacuponera.LacuponeraOfferDetailParser
import com.example.mmapp.app4.data.lacuponera.LacuponeraOffersClient
import com.example.mmapp.app4.data.lacuponera.LacuponeraFreeOffersParser
import com.example.mmapp.app4.domain.scripts.FindFreeLacuponeraProductsScript

class AppContainerFactory(
    private val application: Application,
) {
    fun create(): AppContainer = AppContainer(
        scripts = listOf(
            FindFreeLacuponeraProductsScript(
                client = LacuponeraOffersClient(
                    parser = LacuponeraFreeOffersParser(),
                    detailParser = LacuponeraOfferDetailParser(),
                ),
            ),
        ),
    )
}
