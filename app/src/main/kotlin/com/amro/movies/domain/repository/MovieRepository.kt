package com.amro.movies.domain.repository

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import kotlinx.coroutines.flow.Flow

/**
 * A repository that provides a unified data interface for movie information.
 *
 * It is responsible for fetching data from remote or local sources and
 * mapping it to the domain models.
 */
interface MovieRepository {
    /**
     * Retrieves a list of trending movies from the available data sources.
     *
     * @return A flow that emits a list of [Movie] domain objects.
     */
    fun getTrendingMovies(): Flow<List<Movie>>

    /**
     * Checks if the currently cached trending movies are considered stale.
     *
     * @return True if the data is stale or missing, false otherwise.
     */
    fun isTrendingMoviesStale(): Boolean = false

    /**
     * Retrieves detailed information for a specific movie.
     *
     * @param movieId The unique identifier for the movie.
     * @return A flow that emits the [MovieDetails] domain object.
     */
    fun getMovieDetails(movieId: MovieId): Flow<MovieDetails>
}
