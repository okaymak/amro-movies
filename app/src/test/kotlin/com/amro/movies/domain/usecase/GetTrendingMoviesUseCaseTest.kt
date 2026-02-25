package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTrendingMoviesUseCaseTest {

    @Test
    fun `invoke should return movies from repository`() = runTest {
        // Given
        val movies = listOf(
            Movie(
                id = MovieId.tmdb(1),
                title = "Movie 1",
                overview = "Overview 1",
                posterUrl = "url1",
                genres = emptyList(),
                releaseDate = LocalDate(2023, 1, 1),
                popularity = 100.0
            )
        )
        val repository = object : MovieRepository {
            override fun getTrendingMovies() = flowOf(movies)
        }
        val useCase = GetTrendingMoviesUseCase(repository)

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(movies, result[0])
    }
}
