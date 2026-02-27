package com.amro.movies.features.movie.list

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

        assertEquals(MovieListUiState.Loading, viewModel.state.value)
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

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        
        advanceUntilIdle()

        assertTrue(viewModel.state.value is MovieListUiState.Success)
        val successState = viewModel.state.value as MovieListUiState.Success
        // Default is popularity descending, so Movie A (20.0) should be first
        assertEquals("Movie A", successState.movies[0].title)
        assertEquals("Movie B", successState.movies[1].title)
        assertEquals(SortField.POPULARITY, successState.currentSortField)
        assertEquals(SortDirection.DESCENDING, successState.currentSortDirection)
        job.cancel()
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

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        
        advanceUntilIdle()

        // Change to Title Ascending
        viewModel.onSortFieldSelected(SortField.TITLE)
        viewModel.onSortDirectionSelected(SortDirection.ASCENDING)
        advanceUntilIdle()

        val successState = viewModel.state.value as MovieListUiState.Success
        assertEquals("Movie A", successState.movies[0].title)
        assertEquals("Movie B", successState.movies[1].title)
        assertEquals(SortField.TITLE, successState.currentSortField)
        assertEquals(SortDirection.ASCENDING, successState.currentSortDirection)
        job.cancel()
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

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        
        advanceUntilIdle()

        // Initially both movies are present
        assertEquals(2, (viewModel.state.value as MovieListUiState.Success).movies.size)

        // Select "Action" genre (ID: 1)
        viewModel.onGenreSelected(1)
        advanceUntilIdle()

        val successState = viewModel.state.value as MovieListUiState.Success
        assertEquals(1, successState.movies.size)
        assertEquals("Action Movie", successState.movies[0].title)
        assertTrue(1 in successState.selectedGenres)
        job.cancel()
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

        // Start collecting
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        
        advanceUntilIdle()

        // Apply filter
        viewModel.onGenreSelected(1)
        advanceUntilIdle()
        assertEquals(1, (viewModel.state.value as MovieListUiState.Success).movies.size)

        // Clear filter
        viewModel.onClearFilters()
        advanceUntilIdle()

        val successState = viewModel.state.value as MovieListUiState.Success
        assertEquals(2, successState.movies.size)
        assertTrue(successState.selectedGenres.isEmpty())
        job.cancel()
    }

    @Test
    fun `state should transition to Error when fetch fails`() = runTest {
        val errorMessage = "Network Error"
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flow { throw Exception(errorMessage) }
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

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
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

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
