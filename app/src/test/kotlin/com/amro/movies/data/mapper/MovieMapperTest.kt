package com.amro.movies.data.mapper

import com.amro.movies.data.remote.tmdb.model.TmdbGenreDto
import com.amro.movies.data.remote.tmdb.model.TmdbMovieDetailsDto
import com.amro.movies.data.remote.tmdb.model.TmdbMovieDto
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.model.MovieProvider
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class MovieMapperTest {

    @Test
    fun `toDomain should correctly map DTO to domain model`() {
        // Given
        val dto = TmdbMovieDto(
            id = 123,
            title = "Test Movie",
            overview = "Test Overview",
            posterPath = "/path.jpg",
            genreIds = listOf(1, 2),
            releaseDate = LocalDate(2023, 5, 20),
            popularity = 75.5
        )
        val imageBaseUrl = "https://image.tmdb.org/t/p/w500"
        val genreMap = mapOf(1 to "Action", 2 to "Comedy")

        // When
        val domain = dto.toDomain(imageBaseUrl, genreMap)

        // Then
        assertEquals(MovieId.tmdb(123), domain.id)
        assertEquals(MovieProvider.TMDB, domain.id.provider)
        assertEquals("123", domain.id.rawId)
        assertEquals("Test Movie", domain.title)
        assertEquals("Test Overview", domain.overview)
        assertEquals("https://image.tmdb.org/t/p/w500/path.jpg", domain.posterUrl)
        assertEquals(2, domain.genres.size)
        assertEquals("Action", domain.genres[0].name)
        assertEquals("Comedy", domain.genres[1].name)
        assertEquals(LocalDate(2023, 5, 20), domain.releaseDate)
        assertEquals(75.5, domain.popularity, 0.0)
    }

    @Test
    fun `toDomain should handle unknown genres`() {
        // Given
        val dto = TmdbMovieDto(
            id = 456,
            title = "Unknown Genre Movie",
            overview = "Overview",
            posterPath = "/path.jpg",
            genreIds = listOf(99),
            releaseDate = null,
            popularity = 10.0
        )
        val genreMap = mapOf(1 to "Action")

        // When
        val domain = dto.toDomain("", genreMap)

        // Then
        assertEquals("Unknown", domain.genres[0].name)
        assertEquals(null, domain.releaseDate)
        assertEquals(10.0, domain.popularity, 0.0)
    }

    @Test
    fun `toDomain with details should correctly map DTO to MovieDetails`() {
        // Given
        val dto = TmdbMovieDetailsDto(
            id = 123,
            title = "Detailed Movie",
            tagline = "The best movie ever",
            overview = "Detailed overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            genres = listOf(TmdbGenreDto(1, "Action")),
            releaseDate = LocalDate(2023, 5, 20),
            popularity = 80.0,
            voteAverage = 8.5,
            voteCount = 1000,
            budget = 100000000,
            revenue = 500000000,
            status = "Released",
            imdbId = "tt1234567",
            runtime = 120
        )
        val imageBaseUrl = "https://image.tmdb.org/t/p/w500"
        val imdbBaseUrl = "https://www.imdb.com/title/"

        // When
        val details = dto.toDomain(imageBaseUrl, imdbBaseUrl)

        // Then
        assertEquals(MovieId.tmdb(123), details.movie.id)
        assertEquals("Detailed Movie", details.movie.title)
        assertEquals("The best movie ever", details.tagline)
        assertEquals("https://image.tmdb.org/t/p/w500/backdrop.jpg", details.backdropUrl)
        assertEquals("Detailed overview", details.movie.overview)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", details.movie.posterUrl)
        assertEquals(1, details.movie.genres.size)
        assertEquals("Action", details.movie.genres[0].name)
        assertEquals(LocalDate(2023, 5, 20), details.movie.releaseDate)
        assertEquals(8.5, details.voteAverage, 0.0)
        assertEquals(1000, details.voteCount)
        assertEquals(100000000L, details.budget)
        assertEquals(500000000L, details.revenue)
        assertEquals("Released", details.status)
        assertEquals("https://www.imdb.com/title/tt1234567", details.imdbUrl)
        assertEquals(120.minutes, details.runtime)
    }
}
