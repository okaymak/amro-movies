package com.amro.movies.domain.model

/**
 * A type-safe, provider-aware movie identifier.
 *
 * This value class wraps a string identifier and includes a provider prefix (e.g., "tmdb:123").
 * It allows the application to handle movies from multiple sources (like TMDB and IMDB)
 * interchangeably while maintaining knowledge of their origin.
 *
 * @property id The full identifier string, including the provider prefix.
 */
@JvmInline
value class MovieId(private val id: String) {
    /**
     * The [MovieProvider] associated with this identifier.
     *
     * Determined based on the prefix of the [id] string. Returns [MovieProvider.UNKNOWN]
     * if no recognized prefix is found.
     */
    val provider: MovieProvider
        get() = when {
            id.startsWith(PREFIX_TMDB) -> MovieProvider.TMDB
            id.startsWith(PREFIX_IMDB) -> MovieProvider.IMDB
            else -> MovieProvider.UNKNOWN
        }

    /**
     * The raw identifier string, excluding the provider prefix.
     *
     * If no colon (':') is present in the [id], the full string is returned.
     */
    val rawId: String
        get() = id.substringAfter(':')

    companion object {
        private const val PREFIX_TMDB = "tmdb:"
        private const val PREFIX_IMDB = "imdb:"

        /**
         * Creates a [MovieId] for a TMDB-sourced movie.
         *
         * @param id The numeric TMDB ID.
         * @return A [MovieId] with the "tmdb:" prefix.
         */
        @JvmStatic
        fun tmdb(id: Int) = MovieId(PREFIX_TMDB + id)

        /**
         * Creates a [MovieId] for an IMDB-sourced movie.
         *
         * @param id The IMDB string ID (e.g., "tt1234567").
         * @return A [MovieId] with the "imdb:" prefix.
         */
        @JvmStatic
        fun imdb(id: String) = MovieId(PREFIX_IMDB + id)
    }
}
