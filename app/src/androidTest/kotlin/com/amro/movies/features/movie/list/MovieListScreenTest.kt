package com.amro.movies.features.movie.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import com.amro.movies.ui.theme.AmroTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalSharedTransitionApi::class)
class MovieListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsCircularProgressIndicator() {
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Loading,
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun successState_showsMovies() {
        val movies = persistentListOf(
            Movie(
                id = MovieId.tmdb(1),
                title = "Inception",
                overview = "A thief who steals corporate secrets...",
                posterUrl = "",
                genres = listOf(Genre(1, "Action")),
                releaseDate = LocalDate(2010, 7, 16),
                popularity = 83.0
            )
        )

        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Success(
                                movies = movies,
                                availableGenres = persistentListOf(Genre(1, "Action")),
                                selectedGenres = persistentSetOf(),
                                currentSortField = SortField.POPULARITY,
                                currentSortDirection = SortDirection.DESCENDING,
                                isRefreshing = false
                            ),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Inception").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessageAndRetryButton() {
        val errorMessage = "Network Error"
        
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Error(errorMessage),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessageAndClearFiltersButton() {
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Success(
                                movies = persistentListOf(),
                                availableGenres = persistentListOf(),
                                selectedGenres = persistentSetOf(1),
                                currentSortField = SortField.POPULARITY,
                                currentSortDirection = SortDirection.DESCENDING,
                                isRefreshing = false
                            ),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        // Using substrings as these strings are from R.string and might change
        composeTestRule.onNodeWithText("No movies found", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear filter", substring = true).assertIsDisplayed()
    }

    @Test
    fun clickingSortIcon_opensSortBottomSheet() {
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Success(
                                movies = persistentListOf(),
                                availableGenres = persistentListOf(),
                                selectedGenres = persistentSetOf(),
                                currentSortField = SortField.POPULARITY,
                                currentSortDirection = SortDirection.DESCENDING,
                                isRefreshing = false
                            ),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        // Click sort icon in top bar
        composeTestRule.onNodeWithContentDescription("Sort Movies").performClick()

        // Verify bottom sheet title is displayed
        composeTestRule.onNodeWithText("Sort by").assertIsDisplayed()
        composeTestRule.onNodeWithText("Popularity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Release Date").assertIsDisplayed()
    }

    @Test
    fun clickingSortOption_callsOnSortFieldSelected() {
        val selectedField = AtomicReference<SortField?>(null)
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Success(
                                movies = persistentListOf(),
                                availableGenres = persistentListOf(),
                                selectedGenres = persistentSetOf(),
                                currentSortField = SortField.POPULARITY,
                                currentSortDirection = SortDirection.DESCENDING,
                                isRefreshing = false
                            ),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = { selectedField.set(it) },
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort Movies").performClick()
        composeTestRule.onNodeWithText("Title").performClick()

        assert(selectedField.get() == SortField.TITLE)
    }

    @Test
    fun clickingFilterIcon_opensFilterBottomSheet() {
        val genres = persistentListOf(Genre(1, "Action"), Genre(2, "Comedy"))
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Success(
                                movies = persistentListOf(),
                                availableGenres = genres,
                                selectedGenres = persistentSetOf(),
                                currentSortField = SortField.POPULARITY,
                                currentSortDirection = SortDirection.DESCENDING,
                                isRefreshing = false
                            ),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        // Click filter icon in top bar
        composeTestRule.onNodeWithContentDescription("Filter Movies").performClick()

        // Verify bottom sheet title and genres are displayed
        composeTestRule.onNodeWithText("Filter by Genres").assertIsDisplayed()
        composeTestRule.onNodeWithText("Action").assertIsDisplayed()
        composeTestRule.onNodeWithText("Comedy").assertIsDisplayed()
    }

    @Test
    fun clickingGenreChip_callsOnGenreSelected() {
        val selectedGenreId = AtomicInteger(-1)
        val genres = persistentListOf(Genre(1, "Action"))
        
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Success(
                                movies = persistentListOf(),
                                availableGenres = genres,
                                selectedGenres = persistentSetOf(),
                                currentSortField = SortField.POPULARITY,
                                currentSortDirection = SortDirection.DESCENDING,
                                isRefreshing = false
                            ),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = { selectedGenreId.set(it) },
                            onClearFilters = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Filter Movies").performClick()
        composeTestRule.onNodeWithText("Action").performClick()

        assert(selectedGenreId.get() == 1)
    }

    @Test
    fun clickingClearFilters_callsOnClearFilters() {
        val clearFiltersCalled = AtomicBoolean(false)
        
        composeTestRule.setContent {
            AmroTheme {
                SharedTransitionLayout {
                    AnimatedVisibility(visible = true) {
                        MovieListScreenContent(
                            state = MovieListUiState.Success(
                                movies = persistentListOf(),
                                availableGenres = persistentListOf(),
                                selectedGenres = persistentSetOf(1),
                                currentSortField = SortField.POPULARITY,
                                currentSortDirection = SortDirection.DESCENDING,
                                isRefreshing = false
                            ),
                            onMovieClick = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onRefresh = {},
                            onSortFieldSelect = {},
                            onSortDirectionSelect = {},
                            onGenreSelect = {},
                            onClearFilters = { clearFiltersCalled.set(true) }
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Filter Movies").performClick()
        composeTestRule.onNodeWithText("Clear filter", substring = true).performClick()

        assert(clearFiltersCalled.get())
    }
}
