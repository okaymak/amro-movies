package com.amro.movies.features.movie.list

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.repository.MovieRepository
import com.amro.movies.domain.usecase.GetTrendingMoviesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(emptyList())
        }
        val useCase = GetTrendingMoviesUseCase(repository)
        val viewModel = MovieListViewModel(useCase)

        assertEquals(MovieListUiState.Loading, viewModel.state.value)
    }

    @Test
    fun `state should transition to Success when movies are fetched`() = runTest {
        val movies = listOf(
            Movie(MovieId.tmdb(1), "Movie 1", "Overview 1", "url1", emptyList())
        )
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(movies)
        }
        val useCase = GetTrendingMoviesUseCase(repository)
        val viewModel = MovieListViewModel(useCase)

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        
        advanceUntilIdle()

        assertTrue(viewModel.state.value is MovieListUiState.Success)
        assertEquals(movies, (viewModel.state.value as MovieListUiState.Success).movies)
        job.cancel()
    }

    @Test
    fun `state should transition to Error when fetch fails`() = runTest {
        val errorMessage = "Network Error"
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flow { throw Exception(errorMessage) }
        }
        val useCase = GetTrendingMoviesUseCase(repository)
        val viewModel = MovieListViewModel(useCase)

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        advanceUntilIdle()

        assertTrue(viewModel.state.value is MovieListUiState.Error)
        assertEquals(errorMessage, (viewModel.state.value as MovieListUiState.Error).message)
        job.cancel()
    }

    @Test
    fun `onRefresh should re-fetch movies`() = runTest {
        val fetchCount = AtomicInteger(0)
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flow {
                fetchCount.incrementAndGet()
                emit(emptyList())
            }
        }
        val useCase = GetTrendingMoviesUseCase(repository)
        val viewModel = MovieListViewModel(useCase)

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        
        // Initial fetch
        advanceUntilIdle()
        assertEquals(1, fetchCount.get())

        // Trigger refresh
        viewModel.onRefresh()
        advanceUntilIdle()

        // Verify fetch was called again
        assertEquals(2, fetchCount.get())
        job.cancel()
    }
}
