package com.amro.movies.data.repository

import com.amro.movies.data.mapper.toDomain
import com.amro.movies.data.remote.tmdb.TmdbApi
import com.amro.movies.di.config.TmdbConfig
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.repository.MovieRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * An implementation of [MovieRepository] that fetches data from the TMDB API.
 *
 * This class is responsible for making network calls, mapping the DTOs to domain
 * models, and handling any API-specific logic, such as in-memory caching of genres.
 */
@Inject
class TmdbMovieRepository(
    private val api: TmdbApi,
    private val config: TmdbConfig
) : MovieRepository {
    private val genreCache = mutableMapOf<Int, String>()
    private val genreMutex = Mutex()

    /**
     * Fetches and caches the genre map from the TMDB API.
     *
     * The cache is stored in-memory for the lifetime of the repository and is protected
     * by a [Mutex] to ensure thread-safe access and prevent multiple concurrent fetches.
     *
     * @return A map of genre IDs to their corresponding names.
     */
    private suspend fun getGenres(): Map<Int, String> = genreMutex.withLock {
        if (genreCache.isEmpty()) {
            val genres = api.getGenres()
            genres.forEach {
                genreCache[it.id] = it.name
            }
        }
        genreCache
    }

    /**
     * Fetches the top 100 trending movies from the TMDB API.
     *
     * This method fetches 5 pages of trending movies concurrently (20 items per page)
     * and merges them into a single list. It also ensures that genres are cached
     * before mapping the movie DTOs to domain models.
     *
     * @return A [Flow] emitting a list of 100 unique [Movie] domain models.
     */
    override fun getTrendingMovies(): Flow<List<Movie>> =
        flow {
            val genres = getGenres()

            // Fetch the top 100 movies by making 5 concurrent page requests.
            val movies = coroutineScope {
                (1..5).map { page ->
                    async {
                        api.getTrendingMovies(page)
                            .map {
                                it.toDomain(
                                    imageBaseUrl = config.imageBaseUrl,
                                    genreMap = genres
                                )
                            }
                    }
                }.awaitAll()
                    .flatten()
                    .distinctBy { it.id }
            }
            emit(movies)
        }

    override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flow {
        val tmdbId = movieId.rawId.toIntOrNull()
            ?: throw IllegalArgumentException("Invalid TMDB ID: ${movieId.value}")

        val movieDetails = api.getMovieDetails(tmdbId)
            .toDomain(
                imageBaseUrl = config.imageBaseUrl,
                imdbBaseUrl = config.imdbBaseUrl
            )
        emit(movieDetails)
    }
}
