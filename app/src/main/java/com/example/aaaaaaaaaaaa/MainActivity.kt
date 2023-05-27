package com.example.aaaaaaaaaaaa

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

private const val TAG = "AAAApplication.TAG"
private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
private const val DEFAULT_CITY = "Moscow"

class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    private val service by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
            ).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
        retrofit.create(WeatherService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getWeather(DEFAULT_CITY)
    }

    private fun getWeather(city: String) {
        service.getWeatherByCity(city)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ response ->
                if (response.isSuccessful && response.body() != null) {
                    parseWeatherInfo(response.body()!!)
                } else {
                    // todo show error
                    Toast.makeText(this, "Error: ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                }
            }, { error ->
                // todo show error
                error.printStackTrace()
                Log.e(TAG, "getWeather: ${error.localizedMessage}")
                Toast.makeText(this, "Error: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
            })
            .apply { compositeDisposable.add(this) }
    }

    @SuppressLint("SetTextI18n")
    private fun parseWeatherInfo(weatherInfo: WeatherResponse) {
        weatherInfo.weather?.first()?.description
        val address = findViewById<TextView>(R.id.address)
        val sunrise = findViewById<TextView>(R.id.sunrise)
        val sunset = findViewById<TextView>(R.id.sunset)
        val wind = findViewById<TextView>(R.id.wind)
        val updated_at = findViewById<TextView>(R.id.updated_at)
        val temp_max = findViewById<TextView>(R.id.temp_max)
        val temp_min = findViewById<TextView>(R.id.temp_min)
        val temp = findViewById<TextView>(R.id.temp)
        val pressure = findViewById<TextView>(R.id.pressure)
        val humidity = findViewById<TextView>(R.id.humidity)

        address.text = weatherInfo.name
        wind.text = (weatherInfo.wind?.speed ?: 0).toString() + " м/с"

        val sdf = SimpleDateFormat("HH:mm")
        sunset.text =
            sdf.format(Date(TimeUnit.MILLISECONDS.convert(weatherInfo.sys?.sunset?.toLong() ?: 0L, TimeUnit.SECONDS)))
                .toString()
        sunrise.text =
            sdf.format(Date(TimeUnit.MILLISECONDS.convert(weatherInfo.sys?.sunrise?.toLong() ?: 0L, TimeUnit.SECONDS)))
                .toString()

        updated_at.text = "обновлено в ${sdf.format(Calendar.getInstance().time)}"

        temp_max.text = "Макс. темп. ${weatherInfo.main?.tempMax} °C"
        temp_min.text = "Мин. темп. ${weatherInfo.main?.tempMin} °C"
        temp.text = "${weatherInfo.main?.temp} °C"
        if (!weatherInfo.weather.isNullOrEmpty()) {
            val status = findViewById<TextView>(R.id.status)
            val weather = weatherInfo.weather.first()
            status.text = weather?.description
        }
        pressure.text = "${weatherInfo.main?.pressure}"
        humidity.text = "${weatherInfo.main?.humidity}"
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
}
