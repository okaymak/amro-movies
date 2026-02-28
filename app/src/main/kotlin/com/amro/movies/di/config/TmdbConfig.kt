package com.amro.movies.di.config

import com.amro.movies.BuildConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

data class TmdbConfig(
    val apiBaseUrl: String = "https://api.themoviedb.org/3/",
    val imageBaseUrl: String = "https://image.tmdb.org/t/p/w500",
    val imdbBaseUrl: String = "https://www.imdb.com/title/",
    val bearerToken: String = BuildConfig.TMDB_BEARER_TOKEN,
    val trendingMoviesTtl: Duration = 10.minutes
)
