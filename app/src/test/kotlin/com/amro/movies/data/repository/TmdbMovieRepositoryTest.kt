package com.amro.movies.data.repository

import com.amro.movies.data.remote.tmdb.TmdbApi
import com.amro.movies.di.config.TmdbConfig
import com.amro.movies.domain.model.MovieId
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class TmdbMovieRepositoryTest {

    @Test
    fun `getTrendingMovies should fetch genres only once regardless of sequential or concurrent calls`() = runTest {
        val genreCallCount = AtomicInteger(0)
        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("genre/movie/list")) {
                genreCallCount.incrementAndGet()
                delay(100) // Simulate network delay to test Mutex
                respond(
                    content = """{"genres": [{"id": 1, "name": "Action"}]}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else {
                respond(
                    content = """{"page": 1, "results": [], "total_pages": 1, "total_results": 0}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = TmdbApi(client)
        val repository = TmdbMovieRepository(api, TmdbConfig())

        // Act 1: Fire multiple requests concurrently
        coroutineScope {
            repeat(3) {
                launch { repository.getTrendingMovies().first() }
            }
        }

        // Act 2: Fire a sequential request after the cache should be warm
        repository.getTrendingMovies().first()

        // Assert: Even with concurrent and subsequent calls, genres should only be fetched once
        assertEquals(1, genreCallCount.get())
    }

    @Test
    fun `getTrendingMovies should fetch 100 items in total`() = runTest {
        val requestedPages = mutableListOf<Int>()

        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("genre/movie/list")) {
                respond(
                    content = """{"genres": []}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else {
                val page = request.url.parameters["page"]?.toInt() ?: 1
                requestedPages.add(page)
                
                val results = (1..20).joinToString(",") { i ->
                    """{ "id": ${page * 100 + i}, "title": "Movie ${page * 100 + i}", "overview": "", "poster_path": "", "genre_ids": [], "release_date": "2023-01-01", "popularity": 10.0 }"""
                }

                respond(
                    content = """
                        {
                            "page": $page,
                            "results": [$results],
                            "total_pages": 5,
                            "total_results": 100
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val api = TmdbApi(client)
        val repository = TmdbMovieRepository(api, TmdbConfig())

        // Act
        val movies = repository.getTrendingMovies().first()

        // Assert
        assertEquals(100, movies.size)
        assertEquals(listOf(1, 2, 3, 4, 5), requestedPages.sorted())
        assertEquals(10.0, movies[0].popularity, 0.0)
        assertEquals("2023-01-01", movies[0].releaseDate.toString())
    }

    @Test(expected = Exception::class)
    fun `getTrendingMovies should throw exception if any page fetch fails`() = runTest {
        val mockEngine = MockEngine { request ->
            if (request.url.parameters["page"] == "3") {
                respond(
                    content = "Internal Server Error",
                    status = HttpStatusCode.InternalServerError
                )
            } else {
                respond(
                    content = """{"page": 1, "results": [], "total_pages": 1, "total_results": 0}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = TmdbMovieRepository(TmdbApi(client), TmdbConfig())

        // Act: This should throw an exception because page 3 fails
        repository.getTrendingMovies().first()
    }

    @Test(expected = Exception::class)
    fun `getTrendingMovies should throw exception if genre fetch fails`() = runTest {
        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("genre/movie/list")) {
                respond(
                    content = "Internal Server Error",
                    status = HttpStatusCode.InternalServerError
                )
            } else {
                respond(
                    content = """{"page": 1, "results": [], "total_pages": 1, "total_results": 0}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = TmdbMovieRepository(TmdbApi(client), TmdbConfig())

        // Act: This should throw an exception because genre fetch fails
        repository.getTrendingMovies().first()
    }

    @Test
    fun `getMovieDetails should fetch and map details correctly`() = runTest {
        // Given
        val tmdbId = 123
        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.contains("movie/$tmdbId")) {
                respond(
                    content = """
                        {
                            "id": $tmdbId,
                            "title": "Test Movie",
                            "tagline": "Test Tagline",
                            "overview": "Test Overview",
                            "poster_path": "/poster.jpg",
                            "genres": [{"id": 1, "name": "Action"}],
                            "release_date": "2023-05-20",
                            "popularity": 80.0,
                            "vote_average": 8.5,
                            "vote_count": 100,
                            "budget": 1000,
                            "revenue": 5000,
                            "status": "Released",
                            "imdb_id": "tt123",
                            "runtime": 120
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else {
                respond(content = "Not Found", status = HttpStatusCode.NotFound)
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = TmdbMovieRepository(TmdbApi(client), TmdbConfig())

        // When
        val details = repository.getMovieDetails(MovieId.tmdb(tmdbId)).first()

        // Then
        assertEquals("Test Movie", details.movie.title)
        assertEquals("Test Tagline", details.tagline)
        assertEquals("tt123", details.imdbUrl?.substringAfterLast("/"))
    }
}
