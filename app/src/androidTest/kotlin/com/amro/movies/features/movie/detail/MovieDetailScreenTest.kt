package com.amro.movies.features.movie.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import com.amro.movies.ui.theme.AmroTheme
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalSharedTransitionApi::class)
class MovieDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsCircularProgressIndicator() {
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieDetailScreenContent(
                            state = MovieDetailUiState.Loading,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRetry = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun successState_showsMovieDetails() {
        val details = MovieDetails(
            movie = Movie(
                id = MovieId.tmdb(1),
                title = "Inception",
                overview = "A thief who steals corporate secrets...",
                posterUrl = "",
                genres = listOf(Genre(1, "Action")),
                releaseDate = LocalDate(2010, 7, 16),
                popularity = 83.0
            ),
            tagline = "Your mind is the scene of the crime.",
            backdropUrl = null,
            voteAverage = 8.3,
            voteCount = 34521,
            budget = 160000000,
            revenue = 825532764,
            status = "Released",
            imdbUrl = "https://www.imdb.com/title/tt1375666/",
            runtime = 148.minutes
        )

        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieDetailScreenContent(
                            state = MovieDetailUiState.Success(details),
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRetry = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Inception", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"Your mind is the scene of the crime.\"").assertIsDisplayed()
        composeTestRule.onNodeWithText("A thief who steals corporate secrets...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Action").assertIsDisplayed()
    }

    @Test
    fun successState_whenImdbUrlMissing_doesNotShowImdbButton() {
        val details = MovieDetails(
            movie = Movie(
                id = MovieId.tmdb(1),
                title = "Inception",
                overview = "...",
                posterUrl = "",
                genres = emptyList(),
                releaseDate = null,
                popularity = 0.0
            ),
            tagline = null,
            backdropUrl = null,
            voteAverage = 0.0,
            voteCount = 0,
            budget = 0,
            revenue = 0,
            status = "Released",
            imdbUrl = null,
            runtime = null
        )

        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieDetailScreenContent(
                            state = MovieDetailUiState.Success(details),
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRetry = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("View on IMDB").assertDoesNotExist()
    }

    @Test
    fun successState_showsNotAvailable_forZeroBudgetAndRevenue() {
        val details = MovieDetails(
            movie = Movie(
                id = MovieId.tmdb(1),
                title = "Inception",
                overview = "...",
                posterUrl = "",
                genres = emptyList(),
                releaseDate = null,
                popularity = 0.0
            ),
            tagline = null,
            backdropUrl = null,
            voteAverage = 0.0,
            voteCount = 0,
            budget = 0,
            revenue = 0,
            status = "Released",
            imdbUrl = null,
            runtime = null
        )

        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieDetailScreenContent(
                            state = MovieDetailUiState.Success(details),
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRetry = {}
                        )
                    }
                }
            }
        }

        // In strings.xml, movie_details_not_available is "N/A"
        composeTestRule.onAllNodesWithText("N/A").assertCountEquals(2)
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() {
        val errorMessage = "Could not load movie details"
        val retryCalled = AtomicBoolean(false)
        
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieDetailScreenContent(
                            state = MovieDetailUiState.Error(errorMessage),
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRetry = { retryCalled.set(true) }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").performClick()
        
        assert(retryCalled.get())
    }
}
