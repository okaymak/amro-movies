package com.amro.movies.domain.model

/**
 * A domain-level representation of a movie.
 *
 * This class is provider-agnostic and contains only the core data needed by the
 * application's UI and business logic.
 *
 * @property id The unique, type-safe identifier for the movie.
 * @property title The title of the movie.
 * @property overview A brief summary or description of the movie.
 * @property posterUrl The full URL to the movie's poster image.
 * @property genres The list of genres associated with this movie.
 */
data class Movie(
    val id: MovieId,
    val title: String,
    val overview: String,
    val posterUrl: String,
    val genres: List<Genre>
)
