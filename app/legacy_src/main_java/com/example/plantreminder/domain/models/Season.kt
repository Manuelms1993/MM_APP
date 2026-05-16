package com.example.plantreminder.domain.models

enum class Season {
    PRIMAVERA,
    VERANO,
    OTONO,
    INVIERNO,
    ;

    companion object {
        fun fromMonth(month: Int): Season = when (month) {
            3, 4, 5 -> PRIMAVERA
            6, 7, 8 -> VERANO
            9, 10, 11 -> OTONO
            else -> INVIERNO
        }
    }
}

