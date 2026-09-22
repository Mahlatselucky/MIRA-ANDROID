package com.mira.app.network

/**
 * Builds the single Retrofit instance the whole app uses to reach our REST
 * API, hosted on Render. The base URL points at the live server.
 * See: https://square.github.io/retrofit/
 */

import android.content.Context
import com.mira.app.data.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    
    private const val BASE_URL = "https://mira-android.onrender.com/"

    private var apiServiceInstance: ApiService? = null

    fun init(context: Context) {
        if (apiServiceInstance != null) return

        val tokenManager = TokenManager.getInstance(context)

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = tokenManager.getToken()
            val request = if (token != null) {
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                original
            }
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        apiServiceInstance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val apiService: ApiService
        get() = apiServiceInstance
            ?: throw IllegalStateException("RetrofitClient.init(context) must be called before use")
}
