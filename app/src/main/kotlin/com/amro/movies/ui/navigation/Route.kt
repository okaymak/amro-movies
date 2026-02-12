package com.amro.movies.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Defines the navigation routes available in the application.
 *
 * Each route is represented as a [NavKey] which allows for type-safe navigation
 * and state restoration when using Navigation 3.
 */
sealed interface Route {
    /**
     * Route for the movie list screen, which displays a collection of movies.
     */
    @Serializable
    data object MovieList : NavKey

    /**
     * Route for the movie detail screen, which displays information about a specific movie.
     */
    @Serializable
    data object MovieDetail : NavKey
}
