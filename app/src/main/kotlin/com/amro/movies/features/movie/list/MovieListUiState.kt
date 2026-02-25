package com.amro.movies.features.movie.list

import com.amro.movies.domain.model.Movie

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
     */
    data class Success(val movies: List<Movie>) : MovieListUiState

    /**
     * An error occurred while loading the movie list.
     */
    data class Error(val message: String) : MovieListUiState
}
