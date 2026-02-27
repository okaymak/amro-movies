package com.amro.movies.data.remote.tmdb

import com.amro.movies.data.remote.tmdb.model.TmdbGenreDto
import com.amro.movies.data.remote.tmdb.model.TmdbGenreResponse
import com.amro.movies.data.remote.tmdb.model.TmdbMovieDetailsDto
import com.amro.movies.data.remote.tmdb.model.TmdbMovieDto
import com.amro.movies.data.remote.tmdb.model.TmdbPagedResponse
import com.amro.movies.di.Tmdb
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * A client that encapsulates all API calls to the TMDB service.
 */
@Inject
class TmdbApi(
    @param:Tmdb private val client: HttpClient
) {
    /**
     * Fetches the list of movie genres.
     */
    suspend fun getGenres(): List<TmdbGenreDto> =
        client.get(PATH_GENRES)
            .body<TmdbGenreResponse>()
            .genres

    /**
     * Fetches the list of movies trending this week.
     */
    suspend fun getTrendingMovies(page: Int = 1): List<TmdbMovieDto> =
        client.get(PATH_TRENDING_MOVIES) { parameter(PARAM_PAGE, page) }
            .body<TmdbPagedResponse<TmdbMovieDto>>()
            .results

    /**
     * Fetches detailed information for a specific movie.
     *
     * @param movieId The unique identifier for the movie.
     */
    suspend fun getMovieDetails(movieId: Int): TmdbMovieDetailsDto =
        client.get("movie/$movieId")
            .body<TmdbMovieDetailsDto>()

    private companion object {
        private const val PATH_GENRES = "genre/movie/list"
        private const val PATH_TRENDING_MOVIES = "trending/movie/week"
        private const val PARAM_PAGE = "page"
    }
}
