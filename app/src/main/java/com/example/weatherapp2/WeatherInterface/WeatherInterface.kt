package com.example.weatherapp2.WeatherInterface

import com.example.weatherapp2.Constance
import com.example.weatherapp2.Model.WeatherResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherInterface {

    @GET("2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String?,
        @Query("appid") appid: String?
    ): Call<WeatherResponse>

}

object WeatherService{
    val weatherInstance: WeatherInterface
    init {
        val retrofit=Retrofit.Builder()
            .baseUrl(Constance.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherInstance=retrofit.create(WeatherInterface::class.java)
    }
}