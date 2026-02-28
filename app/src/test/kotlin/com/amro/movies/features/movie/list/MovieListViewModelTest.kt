package com.amro.movies.features.movie.list

import app.cash.turbine.test
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import com.amro.movies.domain.repository.MovieRepository
import com.amro.movies.domain.usecase.FilterMoviesUseCase
import com.amro.movies.domain.usecase.GetTrendingMoviesUseCase
import com.amro.movies.domain.usecase.SortMoviesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val sortMoviesUseCase = SortMoviesUseCase()
    private val filterMoviesUseCase = FilterMoviesUseCase()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: MovieRepository): MovieListViewModel {
        return MovieListViewModel(
            getTrendingMoviesUseCase = GetTrendingMoviesUseCase(repository),
            sortMoviesUseCase = sortMoviesUseCase,
            filterMoviesUseCase = filterMoviesUseCase
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(emptyList())
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state should transition to Success with default sorting when movies are fetched`() = runTest {
        val movies = listOf(
            Movie(MovieId.tmdb(1), "Movie B", "", "", emptyList(), LocalDate(2023, 1, 1), 10.0),
            Movie(MovieId.tmdb(2), "Movie A", "", "", emptyList(), LocalDate(2023, 1, 1), 20.0)
        )
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(movies)
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            val successState = awaitItem() as MovieListUiState.Success
            
            // Default is popularity descending, so Movie A (20.0) should be first
            assertEquals("Movie A", successState.movies[0].title)
            assertEquals("Movie B", successState.movies[1].title)
            assertEquals(SortField.POPULARITY, successState.currentSortField)
            assertEquals(SortDirection.DESCENDING, successState.currentSortDirection)
        }
    }

    @Test
    fun `changing sort field should update state`() = runTest {
        val movies = listOf(
            Movie(MovieId.tmdb(1), "Movie B", "", "", emptyList(), LocalDate(2023, 1, 1), 10.0),
            Movie(MovieId.tmdb(2), "Movie A", "", "", emptyList(), LocalDate(2023, 1, 1), 20.0)
        )
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(movies)
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            assertTrue(awaitItem() is MovieListUiState.Success)

            // Change to Title Ascending
            viewModel.onSortFieldSelected(SortField.TITLE)
            val stateAfterField = awaitItem() as MovieListUiState.Success
            assertEquals(SortField.TITLE, stateAfterField.currentSortField)

            viewModel.onSortDirectionSelected(SortDirection.ASCENDING)
            val successState = awaitItem() as MovieListUiState.Success
            assertEquals("Movie A", successState.movies[0].title)
            assertEquals("Movie B", successState.movies[1].title)
            assertEquals(SortField.TITLE, successState.currentSortField)
            assertEquals(SortDirection.ASCENDING, successState.currentSortDirection)
        }
    }

    @Test
    fun `selecting a genre should filter movies`() = runTest {
        val actionGenre = Genre(1, "Action")
        val comedyGenre = Genre(2, "Comedy")
        val movies = listOf(
            Movie(MovieId.tmdb(1), "Action Movie", "", "", listOf(actionGenre), LocalDate(2023, 1, 1), 10.0),
            Movie(MovieId.tmdb(2), "Comedy Movie", "", "", listOf(comedyGenre), LocalDate(2023, 1, 1), 20.0)
        )
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(movies)
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            val initialSuccess = awaitItem() as MovieListUiState.Success
            assertEquals(2, initialSuccess.movies.size)

            // Select "Action" genre (ID: 1)
            viewModel.onGenreSelected(1)
            
            val successState = awaitItem() as MovieListUiState.Success
            assertEquals(1, successState.movies.size)
            assertEquals("Action Movie", successState.movies[0].title)
            assertTrue(1 in successState.selectedGenres)
        }
    }

    @Test
    fun `clearing filters should restore all movies`() = runTest {
        val actionGenre = Genre(1, "Action")
        val movies = listOf(
            Movie(MovieId.tmdb(1), "Action Movie", "", "", listOf(actionGenre), LocalDate(2023, 1, 1), 10.0),
            Movie(MovieId.tmdb(2), "Other Movie", "", "", emptyList(), LocalDate(2023, 1, 1), 20.0)
        )
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(movies)
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            awaitItem() // Success initial

            // Apply filter
            viewModel.onGenreSelected(1)
            awaitItem() // Filtered state

            // Clear filter
            viewModel.onClearFilters()
            
            val successState = awaitItem() as MovieListUiState.Success
            assertEquals(2, successState.movies.size)
            assertTrue(successState.selectedGenres.isEmpty())
        }
    }

    @Test
    fun `state should transition to Error when fetch fails`() = runTest {
        val errorMessage = "Network Error"
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flow { throw Exception(errorMessage) }
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            val errorState = awaitItem() as MovieListUiState.Error
            assertEquals(errorMessage, errorState.message)
        }
    }

    @Test
    fun `onRefresh should re-fetch movies`() = runTest {
        val fetchCount = AtomicInteger(0)
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flow {
                fetchCount.incrementAndGet()
                emit(emptyList())
            }
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            awaitItem() // Initial Success
            assertEquals(1, fetchCount.get())

            // Trigger refresh
            viewModel.onRefresh()

            // onRefresh triggers isRefreshing=true, which might emit a new state before the actual fetch completes
            val refreshingState = awaitItem() as MovieListUiState.Success
            assertTrue(refreshingState.isRefreshing)

            awaitItem() // Success after refresh
            assertEquals(2, fetchCount.get())
        }
    }

    @Test
    fun `refreshIfStale should trigger refresh when stale and state is Success`() = runTest {
        val fetchCount = AtomicInteger(0)
        var isStale = true
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flow {
                fetchCount.incrementAndGet()
                emit(emptyList())
            }
            override fun isTrendingMoviesStale(): Boolean = isStale
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            awaitItem() // Initial Success from onStart
            assertEquals(1, fetchCount.get())

            // Act: Call refreshIfStale when stale
            viewModel.refreshIfStale()

            // Should trigger refresh
            assertTrue((awaitItem() as MovieListUiState.Success).isRefreshing)
            awaitItem() // Success after refresh
            assertEquals(2, fetchCount.get())

            // Act: Call refreshIfStale when NOT stale
            isStale = false
            viewModel.refreshIfStale()

            // Should NOT trigger refresh (no new state emissions)
            expectNoEvents()
            assertEquals(2, fetchCount.get())
        }
    }

    @Test
    fun `initial load should hit the repository exactly once`() = runTest {
        val fetchCount = AtomicInteger(0)
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flow {
                fetchCount.incrementAndGet()
                emit(emptyList())
            }
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        viewModel.state.test {
            assertEquals(MovieListUiState.Loading, awaitItem())
            assertTrue(awaitItem() is MovieListUiState.Success)

            // The refreshTrigger.onStart { emit(Unit) } ensures exactly one fetch
            assertEquals(1, fetchCount.get())

            // Wait for any potential double-fire
            expectNoEvents()
            assertEquals(1, fetchCount.get())
        }
    }
}
