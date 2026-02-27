package com.amro.movies.features.movie.detail

import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.repository.MovieRepository
import com.amro.movies.domain.usecase.GetMovieDetailsUseCase
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
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val movieId = MovieId.tmdb(123)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: MovieRepository): MovieDetailViewModel {
        return MovieDetailViewModel(
            movieId = movieId,
            getMovieDetailsUseCase = GetMovieDetailsUseCase(repository)
        )
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(emptyList())
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flowOf()
        }
        val viewModel = createViewModel(repository)

        assertEquals(MovieDetailUiState.Loading, viewModel.state.value)
    }

    @Test
    fun `state should transition to Success when movie details are fetched`() = runTest {
        val movie = Movie(
            id = movieId,
            title = "Test Movie",
            overview = "Overview",
            posterUrl = "poster",
            genres = emptyList(),
            releaseDate = LocalDate(2023, 1, 1),
            popularity = 100.0
        )
        val details = MovieDetails(
            movie = movie,
            tagline = "Tagline",
            backdropUrl = "backdrop",
            voteAverage = 8.0,
            voteCount = 100,
            budget = 1000,
            revenue = 2000,
            status = "Released",
            imdbUrl = "imdb",
            runtime = 120.minutes
        )
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(emptyList())
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> {
                return if (movieId == this@MovieDetailViewModelTest.movieId) flowOf(details) else flowOf()
            }
        }
        val viewModel = createViewModel(repository)

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        
        advanceUntilIdle()

        assertTrue(viewModel.state.value is MovieDetailUiState.Success)
        val successState = viewModel.state.value as MovieDetailUiState.Success
        assertEquals(details, successState.details)
        job.cancel()
    }

    @Test
    fun `state should transition to Error when fetch fails`() = runTest {
        val errorMessage = "Network Error"
        val repository = object : MovieRepository {
            override fun getTrendingMovies(): Flow<List<Movie>> = flowOf(emptyList())
            override fun getMovieDetails(movieId: MovieId): Flow<MovieDetails> = flow {
                throw Exception(errorMessage)
            }
        }
        val viewModel = createViewModel(repository)

        // Start collecting to trigger the lazy StateFlow
        val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        advanceUntilIdle()

        assertTrue(viewModel.state.value is MovieDetailUiState.Error)
        assertEquals(errorMessage, (viewModel.state.value as MovieDetailUiState.Error).message)
        job.cancel()
    }
}
