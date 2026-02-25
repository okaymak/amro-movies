package com.amro.movies.features.movie.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.usecase.GetTrendingMoviesUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Movie List screen.
 *
 * Uses [GetTrendingMoviesUseCase] to fetch movies and exposes the UI state via a [StateFlow].
 * Handles refreshing logic.
 */
@Inject
class MovieListViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase
) : ViewModel() {
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MovieListUiState> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getTrendingMoviesUseCase()
                .map<List<Movie>, MovieListUiState> { movies ->
                    MovieListUiState.Success(movies)
                }
                .onStart {
                    emit(MovieListUiState.Loading)
                }
                .catch {
                    emit(MovieListUiState.Error(it.message ?: "Unknown error"))
                }
        }
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
}
