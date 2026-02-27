package com.amro.movies.domain.model

import kotlin.time.Duration

/**
 * A domain-level representation of a movie's detailed information.
 *
 * This class wraps the core [Movie] data and adds additional details
 * required for the movie detail screen.
 *
 * @property movie The core movie information.
 * @property tagline A short tagline for the movie.
 * @property backdropUrl The full URL to the movie's backdrop image.
 * @property voteAverage The average vote score.
 * @property voteCount The number of votes.
 * @property budget The budget of the movie in USD.
 * @property revenue The revenue of the movie in USD.
 * @property status The status of the movie (e.g., Released).
 * @property imdbUrl The full URL to the movie's IMDB page.
 * @property runtime The runtime of the movie.
 */
data class MovieDetails(
    val movie: Movie,
    val tagline: String?,
    val backdropUrl: String?,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: Long,
    val revenue: Long,
    val status: String,
    val imdbUrl: String?,
    val runtime: Duration?
)
