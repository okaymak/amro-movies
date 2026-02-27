package com.amro.movies.data.remote.tmdb.model

import com.amro.movies.data.serialization.EmptyStringLocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data transfer object representing detailed movie information from TMDB.
 */
@Serializable
data class TmdbMovieDetailsDto(
    val id: Int,
    val title: String,
    val tagline: String? = null,
    val overview: String,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    val genres: List<TmdbGenreDto>,
    @Serializable(with = EmptyStringLocalDateSerializer::class)
    @SerialName("release_date")
    val releaseDate: LocalDate? = null,
    val popularity: Double,
    @SerialName("vote_average") 
    val voteAverage: Double,
    @SerialName("vote_count") 
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
    val status: String,
    @SerialName("imdb_id") 
    val imdbId: String? = null,
    val runtime: Int? = null
)
