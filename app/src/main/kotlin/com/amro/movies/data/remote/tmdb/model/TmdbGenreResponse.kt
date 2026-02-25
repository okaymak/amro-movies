package com.amro.movies.data.remote.tmdb.model

import kotlinx.serialization.Serializable

/**
 * API response containing a list of movie genres from TMDB.
 *
 * @property genres The list of [TmdbGenreDto] returned by the API.
 */
@Serializable
data class TmdbGenreResponse(
    val genres: List<TmdbGenreDto>
)
