package com.amro.movies.di.config

import com.amro.movies.BuildConfig

data class TmdbConfig(
    val apiBaseUrl: String = "https://api.themoviedb.org/3/",
    val imageBaseUrl: String = "https://image.tmdb.org/t/p/w500",
    val bearerToken: String = BuildConfig.TMDB_BEARER_TOKEN
)
