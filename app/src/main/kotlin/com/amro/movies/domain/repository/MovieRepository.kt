package com.amro.movies.domain.repository

import com.amro.movies.domain.model.Movie
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
}
