package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class SortMoviesUseCaseTest {

    private val useCase = SortMoviesUseCase()

    private val movies = listOf(
        Movie(MovieId.tmdb(1), "B Movie", "Overview", "url", emptyList(), LocalDate(2023, 1, 1), 50.0),
        Movie(MovieId.tmdb(2), "A Movie", "Overview", "url", emptyList(), LocalDate(2022, 1, 1), 100.0),
        Movie(MovieId.tmdb(3), "C Movie", "Overview", "url", emptyList(), LocalDate(2024, 1, 1), 75.0)
    )

    @Test
    fun `should sort by popularity descending by default`() {
        val result = useCase(movies)
        assertEquals(100.0, result[0].popularity, 0.0)
        assertEquals(75.0, result[1].popularity, 0.0)
        assertEquals(50.0, result[2].popularity, 0.0)
    }

    @Test
    fun `should sort by title ascending`() {
        val result = useCase(movies, SortField.TITLE, SortDirection.ASCENDING)
        assertEquals("A Movie", result[0].title)
        assertEquals("B Movie", result[1].title)
        assertEquals("C Movie", result[2].title)
    }

    @Test
    fun `should sort by title descending`() {
        val result = useCase(movies, SortField.TITLE, SortDirection.DESCENDING)
        assertEquals("C Movie", result[0].title)
        assertEquals("B Movie", result[1].title)
        assertEquals("A Movie", result[2].title)
    }

    @Test
    fun `should sort by popularity ascending`() {
        val result = useCase(movies, SortField.POPULARITY, SortDirection.ASCENDING)
        assertEquals(50.0, result[0].popularity, 0.0)
        assertEquals(75.0, result[1].popularity, 0.0)
        assertEquals(100.0, result[2].popularity, 0.0)
    }

    @Test
    fun `should sort by release date ascending`() {
        val result = useCase(movies, SortField.RELEASE_DATE, SortDirection.ASCENDING)
        assertEquals(LocalDate(2022, 1, 1), result[0].releaseDate)
        assertEquals(LocalDate(2023, 1, 1), result[1].releaseDate)
        assertEquals(LocalDate(2024, 1, 1), result[2].releaseDate)
    }
}
