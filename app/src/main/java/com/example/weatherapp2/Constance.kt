package com.example.weatherapp2

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constance {
    const val BASE_URL: String="https://api.openweathermap.org/data/"
    const val API_ID: String="b0505a48d7e6660d1691b6d1b6a07580"
    const val METRIC_UNIT: String="metric"
    const val PREFERENCE_NAME= "WeatherAppPreference"
    const val WEATHER_RESPONSE_DATA="weather_response_date"
    fun isNetworkAvailable(context: Context):Boolean{
        val connectivityManager=context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val network =connectivityManager.activeNetwork ?: return false
            val activeNetwork=connectivityManager.getNetworkCapabilities(network) ?: return false

            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)-> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)-> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)-> true

                else ->false
            }

        }else{
            val networkInfo=connectivityManager.activeNetworkInfo
            return networkInfo !=null && networkInfo.isConnectedOrConnecting
        }


    }
}