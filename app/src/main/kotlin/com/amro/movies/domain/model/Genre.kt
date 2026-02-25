package com.amro.movies.domain.model

/**
 * A domain-level representation of a movie genre.
 *
 * @property id The unique identifier for the genre.
 * @property name The human-readable name of the genre (e.g., "Action", "Drama").
 */
data class Genre(val id: Int, val name: String)
