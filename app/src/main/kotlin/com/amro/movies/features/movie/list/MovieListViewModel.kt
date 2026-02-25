package com.amro.movies.features.movie.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import com.amro.movies.domain.usecase.GetTrendingMoviesUseCase
import com.amro.movies.domain.usecase.SortMoviesUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Movie List screen.
 *
 * This ViewModel manages the trending movies data by reactively combining the remote
 * data source with in-memory sorting preferences. It exposes a single [state] flow
 * that represents the current UI state.
 *
 * Uses [GetTrendingMoviesUseCase] for data fetching and [SortMoviesUseCase] for
 * applying sorting logic.
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(MovieListViewModel::class)
class MovieListViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val sortMoviesUseCase: SortMoviesUseCase
) : ViewModel() {
    /**
     * A trigger used to re-fetch movies from the remote source.
     */
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    
    /**
     * The current active field for sorting (e.g., Popularity, Title).
     */
    private val sortField = MutableStateFlow(SortField.POPULARITY)

    /**
     * The current active direction for sorting (Ascending/Descending).
     */
    private val sortDirection = MutableStateFlow(SortDirection.DESCENDING)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val trendingMovies = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getTrendingMoviesUseCase()
                .map { Result.success(it) }
                .catch { emit(Result.failure(it)) }
        }

    /**
     * The current state of the Movie List UI.
     *
     * This flow combines the latest trending movies result with the current [sortField]
     * and [sortDirection]. Whenever the movies are refreshed or sorting preferences
     * change, the state is re-emitted with the newly sorted list.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MovieListUiState> = combine(
        trendingMovies,
        sortField,
        sortDirection
    ) { result, field, direction ->
        result.fold(
            onSuccess = { movies ->
                val sortedMovies = sortMoviesUseCase(
                    movies = movies,
                    sortField = field,
                    sortDirection = direction
                )
                MovieListUiState.Success(
                    movies = sortedMovies,
                    currentSortField = field,
                    currentSortDirection = direction
                )
            },
            onFailure = {
                MovieListUiState.Error(it.message ?: "Unknown error")
            }
        )
    }
    .onStart { emit(MovieListUiState.Loading) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = MovieListUiState.Loading
    )

    /**
     * Triggers a refresh of the movie list.
     */
    fun onRefresh() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    /**
     * Updates the field used for sorting the movie list.
     *
     * Changing the field will cause the [state] to re-sort the current list of movies
     * and emit a new [MovieListUiState.Success].
     */
    fun onSortFieldSelected(field: SortField) {
        sortField.value = field
    }

    /**
     * Updates the direction of the sort.
     *
     * Changing the direction will cause the [state] to re-sort the current list of movies
     * and emit a new [MovieListUiState.Success].
     */
    fun onSortDirectionSelected(direction: SortDirection) {
        sortDirection.value = direction
    }
}
