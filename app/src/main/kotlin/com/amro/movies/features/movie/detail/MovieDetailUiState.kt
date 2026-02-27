package com.amro.movies.features.movie.detail

import com.amro.movies.domain.model.MovieDetails

/**
 * Represents the different states for the Movie Detail screen.
 */
sealed interface MovieDetailUiState {
    /**
     * The screen is in a loading state.
     */
    data object Loading : MovieDetailUiState

    /**
     * The movie details were successfully loaded.
     *
     * @property details The detailed movie information.
     */
    data class Success(val details: MovieDetails) : MovieDetailUiState

    /**
     * An error occurred while loading the movie details.
     */
    data class Error(val message: String) : MovieDetailUiState
}
