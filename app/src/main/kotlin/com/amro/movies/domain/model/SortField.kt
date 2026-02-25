package com.amro.movies.domain.model

/**
 * Represents the available fields by which a list of movies can be sorted.
 */
enum class SortField {
    /**
     * Sort by the movie's title.
     */
    TITLE,

    /**
     * Sort by the movie's release date.
     */
    RELEASE_DATE,

    /**
     * Sort by the movie's popularity score.
     */
    POPULARITY
}
