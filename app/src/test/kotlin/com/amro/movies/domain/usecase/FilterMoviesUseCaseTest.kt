package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterMoviesUseCaseTest {

    private val filterMoviesUseCase = FilterMoviesUseCase()

    private val movie1 = Movie(
        id = MovieId.tmdb(1),
        title = "Action Movie",
        overview = "",
        posterUrl = "",
        genres = listOf(Genre(1, "Action"), Genre(2, "Adventure")),
        releaseDate = LocalDate(2023, 1, 1),
        popularity = 10.0
    )

    private val movie2 = Movie(
        id = MovieId.tmdb(2),
        title = "Comedy Movie",
        overview = "",
        posterUrl = "",
        genres = listOf(Genre(3, "Comedy")),
        releaseDate = LocalDate(2023, 1, 1),
        popularity = 20.0
    )

    private val movies = listOf(movie1, movie2)

    @Test
    fun `when genreIds is empty, should return all movies`() {
        val result = filterMoviesUseCase(movies, emptySet())
        assertEquals(movies, result)
    }

    @Test
    fun `when genreId matches, should return matching movies`() {
        val result = filterMoviesUseCase(movies, setOf(1))
        assertEquals(listOf(movie1), result)
    }

    @Test
    fun `when multiple genreIds match, should return movies matching at least one`() {
        val result = filterMoviesUseCase(movies, setOf(1, 3))
        assertEquals(listOf(movie1, movie2), result)
    }

    @Test
    fun `when no genreIds match, should return empty list`() {
        val result = filterMoviesUseCase(movies, setOf(4))
        assertEquals(emptyList<Movie>(), result)
    }
}
