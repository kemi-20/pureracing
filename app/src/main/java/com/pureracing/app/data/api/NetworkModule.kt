package com.pureracing.app.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "auth")
val TOKEN_KEY = stringPreferencesKey("token")

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val token = runBlocking { context.dataStore.data.first()[TOKEN_KEY] }
                val req = if (token != null)
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                else chain.request()
                chain.proceed(req)
            })
            .addInterceptor(HttpLoggingInterceptor { Log.d("OkHttp", it) }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.romielf.com/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): RacingApiService =
        retrofit.create(RacingApiService::class.java)
}
