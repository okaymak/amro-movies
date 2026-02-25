package com.amro.movies.di

import android.app.Application
import com.amro.movies.di.bindings.NetworkBindings
import com.amro.movies.di.bindings.RepositoryBindings
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * The root [DependencyGraph] for the application.
 *
 * This graph is scoped to [AppScope] and serves as the primary container for
 * application-wide dependencies. It implements [MetroAppComponentProviders]
 * for Android component injection and [ViewModelGraph] for Metro-powered
 * ViewModel integration.
 */
@DependencyGraph(
    scope = AppScope::class,
    bindingContainers = [
        NetworkBindings::class,
        RepositoryBindings::class
    ]
)
interface AppGraph : MetroAppComponentProviders, ViewModelGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
