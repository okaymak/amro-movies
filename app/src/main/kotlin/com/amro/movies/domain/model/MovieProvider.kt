package com.amro.movies.domain.model

/**
 * Represents the source of a movie's data, such as TMDB or IMDB.
 *
 * This is used in conjunction with [MovieId] to create a provider-agnostic
 * identifier system, allowing the domain layer to remain decoupled from specific
 * data sources. It supports the application's long-term goal of aggregating
 * data from multiple APIs.
 */
enum class MovieProvider {
    /**
     * Data sourced from The Movie Database (TMDB).
     */
    TMDB,

    /**
     * Data sourced from the Internet Movie Database (IMDB).
     */
    IMDB,

    /**
     * A fallback for unrecognized or unsupported data sources.
     */
    UNKNOWN
}
