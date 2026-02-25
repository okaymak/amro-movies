package com.amro.movies.features.movie.list

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField

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
     * @property movies The sorted list of movies to display.
     * @property currentSortField The field currently used for sorting.
     * @property currentSortDirection The direction of the current sort.
     */
    data class Success(
        val movies: List<Movie>,
        val currentSortField: SortField,
        val currentSortDirection: SortDirection
    ) : MovieListUiState

    /**
     * An error occurred while loading the movie list.
     */
    data class Error(val message: String) : MovieListUiState
}
