package com.amro.movies.features.movie.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.usecase.GetMovieDetailsUseCase
import com.amro.movies.ui.navigation.Route
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
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
 * ViewModel for the Movie Detail screen.
 *
 * This ViewModel is responsible for fetching and managing the data for a single movie
 * and exposing it to the UI through a [StateFlow].
 */
@AssistedInject
class MovieDetailViewModel(
    @Assisted private val movieId: MovieId,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
) : ViewModel() {
    /**
     * A trigger used to re-fetch movie details from the remote source.
     */
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * The current state of the Movie Detail UI.
     *
     * This flow fetches the movie details and transforms the result into the appropriate
     * UI state, handling loading and error cases.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<MovieDetailUiState> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            getMovieDetailsUseCase(movieId)
                .map<_, MovieDetailUiState> { MovieDetailUiState.Success(it) }
                .onStart { emit(MovieDetailUiState.Loading) }
                .catch { emit(MovieDetailUiState.Error(it.message ?: "Unknown error")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = MovieDetailUiState.Loading,
        )

    /**
     * Triggers a refresh of the movie details.
     */
    fun onRetry() {
        viewModelScope.launch {
            refreshTrigger.emit(Unit)
        }
    }

    @AssistedFactory
    @ViewModelAssistedFactoryKey(MovieDetailViewModel::class)
    @ContributesIntoMap(AppScope::class)
    interface Factory : ViewModelAssistedFactory {
        override fun create(extras: CreationExtras): MovieDetailViewModel {
            val movieIdValue = extras[Route.MovieDetail.KEY_MOVIE_ID]
                ?: throw IllegalArgumentException("Movie ID not found in CreationExtras")
            return create(MovieId(movieIdValue))
        }

        fun create(movieId: MovieId): MovieDetailViewModel
    }
}
