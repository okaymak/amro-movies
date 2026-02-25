package com.amro.movies.features.movie.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import com.amro.movies.domain.usecase.FilterMoviesUseCase
import com.amro.movies.domain.usecase.GetTrendingMoviesUseCase
import com.amro.movies.domain.usecase.SortMoviesUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Movie List screen.
 *
 * This ViewModel manages the trending movies data by reactively combining the remote
 * data source with in-memory sorting and filtering preferences. It exposes a single [state] flow
 * that represents the current UI state.
 */
@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(MovieListViewModel::class)
class MovieListViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val sortMoviesUseCase: SortMoviesUseCase,
    private val filterMoviesUseCase: FilterMoviesUseCase
) : ViewModel() {
    /**
     * A trigger used to re-fetch movies from the remote source.
     */
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Whether the list is currently being refreshed.
     */
    private val isRefreshing = MutableStateFlow(false)

    /**
     * The current active field for sorting (e.g., Popularity, Title).
     */
    private val sortField = MutableStateFlow(SortField.POPULARITY)

    /**
     * The current active direction for sorting (Ascending/Descending).
     */
    private val sortDirection = MutableStateFlow(SortDirection.DESCENDING)

    /**
     * The current set of selected genre IDs for filtering.
     */
    private val selectedGenres = MutableStateFlow(emptySet<Int>())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val trendingMovies = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getTrendingMoviesUseCase()
                .map { Result.success(it) }
                .catch { emit(Result.failure(it)) }
        }
        .onEach { isRefreshing.value = false }

    /**
     * The current state of the Movie List UI.
     *
     * This flow combines the latest trending movies result with the current sorting
     * and filtering preferences. Whenever the movies are refreshed, sorted, or
     * filtered, the state is re-emitted.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MovieListUiState> = combine(
        trendingMovies,
        sortField,
        sortDirection,
        selectedGenres,
        isRefreshing
    ) { result, field, direction, genres, refreshing ->
        result.fold(
            onSuccess = { movies ->
                val availableGenres = movies.flatMap { it.genres }.distinctBy { it.id }
                val filteredMovies = filterMoviesUseCase(movies, genres)
                val sortedMovies = sortMoviesUseCase(filteredMovies, field, direction)

                MovieListUiState.Success(
                    movies = sortedMovies.toImmutableList(),
                    availableGenres = availableGenres.toImmutableList(),
                    selectedGenres = genres.toImmutableSet(),
                    currentSortField = field,
                    currentSortDirection = direction,
                    isRefreshing = refreshing
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
            isRefreshing.value = true
            refreshTrigger.emit(Unit)
        }
    }

    /**
     * Updates the field used for sorting.
     */
    fun onSortFieldSelected(field: SortField) {
        sortField.value = field
    }

    /**
     * Updates the direction of the sort.
     */
    fun onSortDirectionSelected(direction: SortDirection) {
        sortDirection.value = direction
    }

    /**
     * Toggles the selection of a genre for filtering.
     */
    fun onGenreSelected(genreId: Int) {
        selectedGenres.value = if (genreId in selectedGenres.value) {
            selectedGenres.value - genreId
        } else {
            selectedGenres.value + genreId
        }
    }

    /**
     * Clears all active genre filters.
     */
    fun onClearFilters() {
        selectedGenres.value = emptySet()
    }
}
