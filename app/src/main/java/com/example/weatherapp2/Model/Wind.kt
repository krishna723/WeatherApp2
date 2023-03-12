package com.example.weatherapp2.Model

import java.io.Serializable

data class Wind(
    val deg: Int,
    val gust: Double,
    val speed: Double
): Serializable