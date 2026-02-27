package com.amro.movies.ui.navigation

import androidx.lifecycle.viewmodel.CreationExtras
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
     *
     * @property movieId The identifier for the movie to display.
     */
    @Serializable
    data class MovieDetail(val movieId: String) : NavKey {
        companion object {
            /**
             * Key used to store the movie ID in [CreationExtras].
             */
            val KEY_MOVIE_ID = CreationExtras.Key<String>()
        }
    }
}
