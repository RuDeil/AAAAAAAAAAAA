package com.example.aaaaaaaaaaaa

import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

private const val apiKey = "ea78610280af633a7578b4b1f22ee616"
private const val UNITS = "metric"
private const val LANG = "ru"

interface WeatherService {

    @GET("weather")
    fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") appid: String = apiKey,
        @Query("units") units: String = UNITS,
        @Query("lang") lang: String = LANG,
    ): Single<Response<WeatherResponse>>
}
