package com.amro.movies

import android.app.Application
import com.amro.movies.di.AppGraph
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication

/**
 * Main [Application] class for the AMRO Movies app.
 *
 * This class implements [MetroApplication] to enable dependency injection via Metro.
 * It serves as the entry point for the application and handles the setup of
 * application-scoped components.
 */
class MoviesApplication : Application(), MetroApplication {
    private val appGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(this)
    }

    override val appComponentProviders: MetroAppComponentProviders
        get() = appGraph
}
