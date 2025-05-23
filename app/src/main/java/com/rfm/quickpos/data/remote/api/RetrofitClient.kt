// app/src/main/java/com/rfm/quickpos/data/remote/api/RetrofitClient.kt

package com.rfm.quickpos.data.remote.api

import com.rfm.quickpos.data.local.storage.SecureCredentialStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client for API communication
 */
object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.63:3000/" // Replace with your API URL

    /**
     * Create a retrofit client instance
     */
    fun create(credentialStore: SecureCredentialStore): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor(credentialStore))
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    /**
     * Create auth interceptor to add token to requests
     */
    private fun createAuthInterceptor(credentialStore: SecureCredentialStore): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()

            // Get auth token if available
            val token = credentialStore.getAuthToken()

            // Add token to request if available
            val request = if (token != null) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(request)
        }
    }

    /**
     * Create logging interceptor for debugging
     */
    private fun createLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
}