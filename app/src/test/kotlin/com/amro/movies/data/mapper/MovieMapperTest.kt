package com.amro.movies.data.mapper

import com.amro.movies.data.remote.tmdb.model.TmdbMovieDto
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.model.MovieProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class MovieMapperTest {

    @Test
    fun `toDomain should correctly map DTO to domain model`() {
        // Given
        val dto = TmdbMovieDto(
            id = 123,
            title = "Test Movie",
            overview = "Test Overview",
            posterPath = "/path.jpg",
            genreIds = listOf(1, 2)
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
    }

    @Test
    fun `toDomain should handle unknown genres`() {
        // Given
        val dto = TmdbMovieDto(
            id = 456,
            title = "Unknown Genre Movie",
            overview = "Overview",
            posterPath = "/path.jpg",
            genreIds = listOf(99)
        )
        val genreMap = mapOf(1 to "Action")

        // When
        val domain = dto.toDomain("", genreMap)

        // Then
        assertEquals("Unknown", domain.genres[0].name)
    }
}
