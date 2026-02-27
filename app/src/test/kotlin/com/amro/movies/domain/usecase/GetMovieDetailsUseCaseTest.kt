package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class GetMovieDetailsUseCaseTest {

    @Test
    fun `invoke should return movie details from repository`() = runTest {
        // Given
        val expectedMovieId = MovieId.tmdb(123)
        val movie = Movie(
            id = expectedMovieId,
            title = "Test Movie",
            overview = "Overview",
            posterUrl = "url",
            genres = emptyList(),
            releaseDate = LocalDate(2023, 1, 1),
            popularity = 100.0
        )
        val details = MovieDetails(
            movie = movie,
            tagline = "Tagline",
            backdropUrl = "backdrop",
            voteAverage = 8.0,
            voteCount = 100,
            budget = 1000,
            revenue = 2000,
            status = "Released",
            imdbUrl = "imdb",
            runtime = 120.minutes
        )
        
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(emptyList())
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> {
                return if (movieId == expectedMovieId) flowOf(details) else flowOf()
            }
        }
        val useCase = GetMovieDetailsUseCase(repository)

        // When
        val result = useCase(expectedMovieId).toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(details, result[0])
    }
}
