package com.jeison.agrocontrol.network

import com.jeison.agrocontrol.BuildConfig
import com.jeison.agrocontrol.auth.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    /**
     * Backend en producción (Render): https://agrocontrol-apjl.onrender.com/api/v1/
     * Se define en app/build.gradle.kts como BuildConfig.API_BASE_URL.
     */
    val BASE_URL: String = BuildConfig.API_BASE_URL

    // Render free duerme el servicio tras ~15 min sin tráfico: el primer
    // request puede tardar 30-60 s en despertar.
    private const val TIMEOUT_SECONDS = 60L

    private val refreshApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build(),
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun createApi(tokenStore: TokenStore): ApiService {
        val authInterceptor = Interceptor { chain ->
            val token = tokenStore.getAccessToken()
            val request = if (!token.isNullOrBlank()) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        val authenticator = object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                if (responseCount(response) >= 2) return null
                val refreshToken = tokenStore.getRefreshToken() ?: run {
                    tokenStore.clear()
                    return null
                }
                val tokens = runBlocking {
                    runCatching {
                        refreshApi.refresh(RefreshRequest(refreshToken))
                    }.getOrNull()
                } ?: run {
                    tokenStore.clear()
                    return null
                }
                tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${tokens.accessToken}")
                    .build()
            }

            private fun responseCount(response: Response): Int {
                var count = 1
                var prior = response.priorResponse
                while (prior != null) {
                    count++
                    prior = prior.priorResponse
                }
                return count
            }
        }
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(authenticator)
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
