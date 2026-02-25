package com.amro.movies.features.movie.list

import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

/**
 * Represents the different states for the Movie List screen.
 */
sealed interface MovieListUiState {
    /**
     * The screen is in a loading state.
     */
    data object Loading : MovieListUiState

    /**
     * The movie list was successfully loaded.
     *
     * @property movies The filtered and sorted list of movies to display.
     * @property availableGenres The list of all unique genres available for filtering.
     * @property selectedGenres The IDs of the currently selected genres for filtering.
     * @property currentSortField The field by which the movies are currently sorted.
     * @property currentSortDirection The direction of the current sort.
     * @property isRefreshing Whether the list is currently being refreshed.
     */
    data class Success(
        val movies: ImmutableList<Movie>,
        val availableGenres: ImmutableList<Genre>,
        val selectedGenres: ImmutableSet<Int>,
        val currentSortField: SortField,
        val currentSortDirection: SortDirection,
        val isRefreshing: Boolean = false
    ) : MovieListUiState

    /**
     * An error occurred while loading the movie list.
     */
    data class Error(val message: String) : MovieListUiState
}
