package com.amro.movies.data.remote.tmdb.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A generic API response for paginated lists from TMDB.
 *
 * @param T The type of results contained in the response.
 * @property page The current page number.
 * @property results The list of results of type [T].
 * @property totalPages The total number of pages available.
 * @property totalResults The total number of items available across all pages.
 */
@Serializable
data class TmdbPagedResponse<T>(
    val page: Int,
    val results: List<T>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)
