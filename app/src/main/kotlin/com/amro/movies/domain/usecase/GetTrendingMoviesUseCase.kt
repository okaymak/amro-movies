package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.repository.MovieRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * A use case that retrieves the top trending movies for the current period.
 *
 * This encapsulates the business logic for fetching movies, allowing the UI to
 * remain independent of where the data is coming from.
 */
@Inject
class GetTrendingMoviesUseCase(
    private val repository: MovieRepository
) {
    /**
     * Executes the use case.
     *
     * @return A flow that emits a list of trending [Movie] objects.
     */
    operator fun invoke(): Flow<List<Movie>> = repository.getTrendingMovies()

    /**
     * Checks if the trending movies data is stale.
     *
     * @return True if data is stale, false otherwise.
     */
    fun isStale(): Boolean = repository.isTrendingMoviesStale()
}
