package com.amro.movies.data.remote.tmdb.model

import kotlinx.serialization.Serializable

/**
 * Data transfer object representing a movie genre from TMDB.
 *
 * @property id The unique identifier for the genre.
 * @property name The name of the genre.
 */
@Serializable
data class TmdbGenreDto(
    val id: Int,
    val name: String
)
