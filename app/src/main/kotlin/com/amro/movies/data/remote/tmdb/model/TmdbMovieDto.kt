package com.amro.movies.data.remote.tmdb.model

import com.amro.movies.data.serialization.EmptyStringLocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing a movie from TMDB.
 *
 * @property id The unique identifier for the movie.
 * @property title The title of the movie.
 * @property overview A brief summary or description of the movie.
 * @property posterPath The relative path to the movie's poster image.
 * @property genreIds The list of genre identifiers associated with this movie.
 * @property releaseDate The release date of the movie.
 * @property popularity The popularity score of the movie.
 */
@Serializable
data class TmdbMovieDto(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path") val posterPath: String,
    @SerialName("genre_ids") val genreIds: List<Int>,
    @Serializable(with = EmptyStringLocalDateSerializer::class)
    @SerialName("release_date") val releaseDate: LocalDate? = null,
    val popularity: Double = 0.0
)
