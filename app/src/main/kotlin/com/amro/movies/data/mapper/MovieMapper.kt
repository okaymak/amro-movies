package com.amro.movies.data.mapper

import com.amro.movies.data.remote.tmdb.model.TmdbMovieDto
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId

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
