package com.amro.movies.domain.usecase

import com.amro.movies.domain.model.Movie
import dev.zacsweers.metro.Inject

/**
 * A use case that filters a list of movies by their associated genres.
 */
@Inject
class FilterMoviesUseCase {

    /**
     * Filters the given list of movies.
     *
     * If [genreIds] is empty, the original list is returned. Otherwise, only movies
     * that contain ALL of the specified genre IDs are returned.
     *
     * @param movies The list of movies to filter.
     * @param genreIds The IDs of the genres to filter by.
     * @return A filtered list of movies.
     */
    operator fun invoke(movies: List<Movie>, genreIds: Set<Int>): List<Movie> {
        if (genreIds.isEmpty()) return movies

        return movies.filter { movie ->
            val movieGenreIds = movie.genres.map { it.id }
            genreIds.all { selectedId -> selectedId in movieGenreIds }
        }
    }
}
