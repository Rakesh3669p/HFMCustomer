package com.hfm.customer.di

import android.content.Context
import com.hfm.customer.HFMCustomer
import com.hfm.customer.data.HFMCustomerAPI
import com.hfm.customer.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.hfm.customer.data.Repository
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Singleton
    @Provides
    fun provideRepository(hFMCustomerAPI: HFMCustomerAPI) = Repository(hFMCustomerAPI)


    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context) = SessionManager(context)

    @Singleton
    @Provides
    fun provideMainApplication() = HFMCustomer()


    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(context.cacheDir, cacheSize.toLong())

        return OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .connectionPool(ConnectionPool())
            .addInterceptor(logging)
            .cache(cache) // Add cache to OkHttpClient
            .build()
    }

    @Provides
    @Singleton
    fun provideTaleometerAPI(okHttpClient: OkHttpClient): HFMCustomerAPI {
        return Retrofit.Builder()
            .baseUrl(HFMCustomerAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Set the OkHttpClient instance
            .build()
            .create(HFMCustomerAPI::class.java) // Specify the API service interface
    }

}