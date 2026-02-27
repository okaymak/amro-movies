package com.amro.movies.data.mapper

import com.amro.movies.data.remote.tmdb.model.TmdbMovieDetailsDto
import com.amro.movies.data.remote.tmdb.model.TmdbMovieDto
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import kotlin.time.Duration.Companion.minutes

/**
 * Maps a [TmdbMovieDto] from the data layer to a [Movie] in the domain layer.
 *
 * @param imageBaseUrl The base URL for constructing the full poster image path.
 * @param genreMap A map of genre IDs to their names, used to resolve genre information.
 * @return A domain-level [Movie] object.
 */
fun TmdbMovieDto.toDomain(
    imageBaseUrl: String,
    genreMap: Map<Int, String>
): Movie = Movie(
    id = MovieId.tmdb(id),
    title = this.title,
    overview = this.overview,
    posterUrl = "${imageBaseUrl}${posterPath}",
    genres = genreIds.map { id ->
        Genre(id = id, name = genreMap[id] ?: "Unknown")
    },
    releaseDate = this.releaseDate,
    popularity = this.popularity
)

/**
 * Maps a [TmdbMovieDetailsDto] from the data layer to a [MovieDetails] in the domain layer.
 *
 * @param imageBaseUrl The base URL for constructing the full poster image path.
 * @param imdbBaseUrl The base URL for constructing the full IMDB movie path.
 * @return A domain-level [MovieDetails] object.
 */
fun TmdbMovieDetailsDto.toDomain(
    imageBaseUrl: String,
    imdbBaseUrl: String
): MovieDetails {
    val movie = Movie(
        id = MovieId.tmdb(id),
        title = title,
        overview = overview,
        posterUrl = posterPath?.let { "${imageBaseUrl}$it" } ?: "",
        genres = genres.map { Genre(it.id, it.name) },
        releaseDate = releaseDate,
        popularity = popularity
    )
    return MovieDetails(
        movie = movie,
        tagline = tagline,
        backdropUrl = backdropPath?.let { "${imageBaseUrl}$it" },
        voteAverage = voteAverage,
        voteCount = voteCount,
        budget = budget,
        revenue = revenue,
        status = status,
        imdbUrl = imdbId?.let { "${imdbBaseUrl}$it" },
        runtime = runtime?.minutes
    )
}
