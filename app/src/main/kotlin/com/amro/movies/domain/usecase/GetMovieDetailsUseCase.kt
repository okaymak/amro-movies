package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.repository.MovieRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow

/**
 * A use case that retrieves detailed information for a specific movie.
 */
@Inject
class GetMovieDetailsUseCase(
    private val repository: MovieRepository
) {
    /**
     * Executes the use case to fetch movie details.
     *
     * @param movieId The unique identifier for the movie.
     * @return A flow that emits the [MovieDetails] domain object.
     */
    operator fun invoke(movieId: MovieId): Flow<MovieDetails> =
        repository.getMovieDetails(movieId)
}
