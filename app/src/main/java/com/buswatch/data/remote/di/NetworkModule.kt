// ABOUTME: Hilt module providing network dependencies (Retrofit, OkHttp, API service)
package com.buswatch.data.remote.di

import com.buswatch.BuildConfig
import com.buswatch.data.remote.TfLApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val apiKeyInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            val urlWithApiKey = originalRequest.url.newBuilder()
                .addQueryParameter("app_key", BuildConfig.TFL_API_KEY)
                .build()
            val requestWithApiKey = originalRequest.newBuilder()
                .url(urlWithApiKey)
                .build()
            chain.proceed(requestWithApiKey)
        }

        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.tfl.gov.uk/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTfLApiService(retrofit: Retrofit): TfLApiService {
        return retrofit.create(TfLApiService::class.java)
    }
}
