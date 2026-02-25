package com.amro.movies.data.remote

import com.amro.movies.data.remote.tmdb.TmdbApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class TmdbApiTest {

    @Test
    fun `getTrendingMovies should return list of movies from JSON`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "page": 1,
                        "results": [
                            {
                                "id": 1,
                                "title": "Movie 1",
                                "overview": "Overview 1",
                                "poster_path": "/path1.jpg",
                                "genre_ids": [12, 18]
                            }
                        ],
                        "total_pages": 1,
                        "total_results": 1
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = TmdbApi(client)

        // When
        val result = api.getTrendingMovies(1)

        // Then
        assertEquals(1, result.size)
        assertEquals(1, result[0].id)
        assertEquals("Movie 1", result[0].title)
    }

    @Test
    fun `getGenres should return list of genres from JSON`() = runTest {
        // Given
        val mockEngine = MockEngine { request ->
            respond(
                content = """
                    {
                        "genres": [
                            { "id": 28, "name": "Action" },
                            { "id": 12, "name": "Adventure" }
                        ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = TmdbApi(client)

        // When
        val result = api.getGenres()

        // Then
        assertEquals(2, result.size)
        assertEquals("Action", result[0].name)
    }
}
