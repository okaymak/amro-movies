package com.amro.movies.di.bindings

import android.util.Log
import com.amro.movies.BuildConfig
import com.amro.movies.di.Base
import com.amro.movies.di.Tmdb
import com.amro.movies.di.config.TmdbConfig
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

@BindingContainer
object NetworkBindings {
    @Provides
    fun provideTmdbConfig(): TmdbConfig = TmdbConfig()

    @Provides
    fun provideJson(): Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    @Base
    fun provideBaseHttpClient(json: Json): HttpClient =
        HttpClient(engineFactory = OkHttp) {
            install(plugin = ContentNegotiation) {
                json(json = json)
            }

            install(plugin = HttpCache)

            if (BuildConfig.DEBUG) {
                install(plugin = Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d("KtorClient", message)
                        }
                    }
                    level = LogLevel.INFO
                }
            }
        }

    @Provides
    @Tmdb
    fun provideTmdbHttpClient(@Base baseClient: HttpClient, config: TmdbConfig): HttpClient =
        baseClient.config {
            defaultRequest {
                url(config.apiBaseUrl)
                header("Authorization", "Bearer ${config.bearerToken}")
            }
        }
}
