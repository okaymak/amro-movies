package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import dev.zacsweers.metro.Inject

/**
 * A use case that applies sorting to a list of movies.
 *
 * This operation is performed in-memory on the provided list.
 */
@Inject
class SortMoviesUseCase {

    /**
     * Sorts the given list of movies.
     *
     * @param movies The list of movies to sort.
     * @param sortField The field by which to sort the movies. Defaults to [SortField.POPULARITY].
     * @param sortDirection The direction of the sort. Defaults to [SortDirection.DESCENDING].
     * @return A new list containing the movies sorted as requested.
     */
    operator fun invoke(
        movies: List<Movie>,
        sortField: SortField = SortField.POPULARITY,
        sortDirection: SortDirection = SortDirection.DESCENDING
    ): List<Movie> {
        val sortedMovies = when (sortField) {
            SortField.TITLE -> movies.sortedBy { it.title.lowercase() }
            SortField.RELEASE_DATE -> movies.sortedWith(compareBy(nullsLast()) { it.releaseDate })
            SortField.POPULARITY -> movies.sortedBy { it.popularity }
        }

        return if (sortDirection == SortDirection.DESCENDING) {
            sortedMovies.reversed()
        } else {
            sortedMovies
        }
    }
}
