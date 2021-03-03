package com.hoc.flowmvi.data

import com.hoc.flowmvi.data.remote.UserApiService
import com.hoc.flowmvi.domain.repository.UserRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Retention(AnnotationRetention.BINARY)
@Qualifier
private annotation class BaseUrl

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
  @ExperimentalCoroutinesApi
  @ExperimentalTime
  @Binds
  @Singleton
  abstract fun userRepository(impl: UserRepositoryImpl): UserRepository

  internal companion object {
    @Provides
    @Singleton
    fun userApiService(retrofit: Retrofit): UserApiService = UserApiService(retrofit = retrofit)

    @Provides
    @Singleton
    fun retrofit(
      @BaseUrl baseUrl: String,
      moshi: Moshi,
      client: OkHttpClient,
    ): Retrofit =
      provideRetrofit(
        baseUrl = baseUrl,
        moshi = moshi,
        client = client,
      )

    @Provides
    @Singleton
    fun moshi(): Moshi = provideMoshi()

    @Provides
    @Singleton
    fun okHttpClient(): OkHttpClient = provideOkHttpClient()

    @Provides
    @BaseUrl
    fun baseUrl(): String = "https://mvi-coroutines-flow-server.herokuapp.com/"
  }
}

private fun provideMoshi(): Moshi {
  return Moshi
    .Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
}

private fun provideRetrofit(baseUrl: String, moshi: Moshi, client: OkHttpClient): Retrofit {
  return Retrofit.Builder()
    .client(client)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(baseUrl)
    .build()
}

private fun provideOkHttpClient(): OkHttpClient {
  return OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(10, TimeUnit.SECONDS)
    .writeTimeout(10, TimeUnit.SECONDS)
    .addInterceptor(
      HttpLoggingInterceptor()
        .apply { level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE }
    )
    .build()
}
