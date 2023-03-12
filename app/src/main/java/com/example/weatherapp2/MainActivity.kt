package com.example.weatherapp2

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.weatherapp2.Model.WeatherResponse
import com.example.weatherapp2.WeatherInterface.WeatherService
import com.example.weatherapp2.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (!isLocationEnabled()) {
            Toast.makeText(this, "Location turned off. Please turn it on", Toast.LENGTH_LONG).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withContext(this)
                .withPermissions(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "You have denied location permission. Please enable them as it is mandatory for app to work",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermission()
                    }

                }).onSameThread()
                .check()
        }

    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        Log.d("latitude", "hi")
        mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            val location: Location? = task.result
            if (location != null) {
                val latitude: Double = location.latitude
                val longitude: Double = location.longitude
                Log.d("lat", latitude.toString())
                Log.d("lon", longitude.toString())
                getLocationWeather(latitude, longitude)
            }

        }
    }

    private fun getLocationWeather(lat: Double, lon: Double) {

        if (Constance.isNetworkAvailable(this)) {
            val weatherList: Call<WeatherResponse> = WeatherService.weatherInstance.getWeather(
                lat,
                lon,
                Constance.METRIC_UNIT,
                Constance.API_ID
            )

            weatherList.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val weatherList: WeatherResponse? = response.body()
                        if (weatherList != null) {
                            setupUI(weatherList)
                        }
                        Log.d("weather", weatherList?.weather.toString())
                    } else {
                        val rc = response.code()
                        when (rc) {
                            400 -> {
                                Log.e("Error 400", "Bad Connection")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }

                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Errorr", t.message.toString())
                }

            })

        } else {
            Toast.makeText(this@MainActivity, "Turn on the internet connection", Toast.LENGTH_LONG)
                .show()
        }

    }


//    private val mLocationCallback=object : LocationCallback(){
//        override fun onLocationResult(locationResult: LocationResult) {
//            val mLastLocation: Location? =locationResult.lastLocation
//            val latitude=mLastLocation?.latitude
//            Log.i("Latitude",latitude.toString())
//            val longitude=mLastLocation?.longitude
//            Log.i("Longitude",longitude.toString())
////            getLocationWeatherDetails(latitude!!,longitude!!)
//
//        }
//    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }


    private fun setupUI(weatherList: WeatherResponse) {
        for (i in weatherList.weather.indices) {
            Log.i("indicate", i.toString())
            binding?.tvMain?.text = weatherList.weather[i].main
            binding?.tvMainDescription?.text = weatherList.weather[i].description
            binding?.tvTemp?.text=weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
            binding?.tvSunriseTime?.text=unixTime(weatherList.sys.sunrise.toLong())
            binding?.tvSunsetTime?.text=unixTime(weatherList.sys.sunset.toLong())
        }

    }

    private fun getUnit(value: String): String? {

        var value = "°C"

        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value

    }
    private fun unixTime(timex: Long): String?{
        val date=Date(timex* 1000L)
        val sdf= SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone= TimeZone.getDefault()
        return sdf.format(date)
    }

    //Rational permission custom dialog
    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enable under Application Settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()


    }

    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}
