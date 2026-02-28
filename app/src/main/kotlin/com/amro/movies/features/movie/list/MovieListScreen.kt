package com.amro.movies.features.movie.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import com.amro.movies.R
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import com.amro.movies.features.movie.list.components.FilterBottomSheet
import com.amro.movies.features.movie.list.components.SortBottomSheet
import com.amro.movies.ui.theme.AmroTheme
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate

/**
 * Main screen for the Movie List feature.
 * Displays a grid of trending movies with support for pull-to-refresh, sorting, and filtering.
 *
 * @param onMovieClick Called when a movie is selected.
 * @param sharedTransitionScope The shared transition scope for poster transitions.
 * @param animatedVisibilityScope The animated visibility scope for poster transitions.
 * @param modifier Screen modifier.
 * @param viewModel The view model managing the screen state.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MovieListScreen(
    onMovieClick: (Movie) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    viewModel: MovieListViewModel = metroViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isReady = state is MovieListUiState.Success
    LifecycleResumeEffect(isReady) {
        if (isReady) {
            viewModel.refreshIfStale()
        }
        onPauseOrDispose { }
    }

    MovieListScreenContent(
        state = state,
        onMovieClick = onMovieClick,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onRefresh = viewModel::onRefresh,
        onSortFieldSelect = viewModel::onSortFieldSelected,
        onSortDirectionSelect = viewModel::onSortDirectionSelected,
        onGenreSelect = viewModel::onGenreSelected,
        onClearFilters = viewModel::onClearFilters,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MovieListScreenContent(
    state: MovieListUiState,
    onMovieClick: (Movie) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRefresh: () -> Unit,
    onSortFieldSelect: (SortField) -> Unit,
    onSortDirectionSelect: (SortDirection) -> Unit,
    onGenreSelect: (Int) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val minCellSize = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)) {
        120.dp
    } else {
        160.dp
    }
    
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Keep track of the last applied filter/sort state to avoid jumping to top when returning from details.
    // We only want to scroll to top when the user explicitly changes sorting or filtering.
    var lastAppliedFilterKey by rememberSaveable { mutableStateOf<String?>(null) }

    // Dynamic app bar alpha based on scroll position
    val appBarAlpha by remember {
        derivedStateOf {
            if (gridState.firstVisibleItemIndex > 0) {
                0.90f
            } else {
                (gridState.firstVisibleItemScrollOffset / 80f).coerceIn(0f, 0.90f)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.title_trending_movies))
                },
                actions = {
                    if (state is MovieListUiState.Success) {
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_sort_24),
                                contentDescription = stringResource(R.string.action_sort_movies),
                            )
                        }

                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_filter_list_24px),
                                contentDescription = stringResource(R.string.action_filter_movies),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = appBarAlpha),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = appBarAlpha),
                ),
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state is MovieListUiState.Success && state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
            Crossfade(targetState = state, label = "MovieListState") { currentState ->
                when (currentState) {
                    is MovieListUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding()),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.testTag("LoadingIndicator"))
                        }
                    }
                    is MovieListUiState.Error -> {
                        ErrorState(
                            message = currentState.message,
                            onRetry = onRefresh,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding()),
                        )
                    }
                    is MovieListUiState.Success -> {
                        val currentFilterKey = remember(currentState.currentSortField, currentState.currentSortDirection, currentState.selectedGenres) {
                            "${currentState.currentSortField.name}-${currentState.currentSortDirection.name}-${currentState.selectedGenres.sorted().joinToString(",")}"
                        }

                        LaunchedEffect(currentFilterKey) {
                            // If this is not the first load and the filter key has changed, scroll to top.
                            if (lastAppliedFilterKey != null && lastAppliedFilterKey != currentFilterKey) {
                                gridState.scrollToItem(0)
                            }
                            lastAppliedFilterKey = currentFilterKey
                        }

                        if (currentState.movies.isEmpty()) {
                            EmptyState(
                                onClearFilters = onClearFilters,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = innerPadding.calculateTopPadding()),
                            )
                        } else {
                            LazyVerticalGrid(
                                state = gridState,
                                columns = GridCells.Adaptive(minSize = minCellSize),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    top = innerPadding.calculateTopPadding() + 16.dp,
                                    end = 16.dp,
                                    bottom = 16.dp,
                                ),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                items(currentState.movies, key = { it.id.value }) { movie ->
                                    MovieItem(
                                        movie = movie,
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        onClick = { onMovieClick(movie) },
                                    )
                                }
                            }
                        }

                        if (showSortSheet) {
                            SortBottomSheet(
                                currentField = currentState.currentSortField,
                                currentDirection = currentState.currentSortDirection,
                                onFieldSelect = onSortFieldSelect,
                                onDirectionSelect = {
                                    onSortDirectionSelect(it)
                                    showSortSheet = false
                                },
                                onDismiss = { showSortSheet = false },
                            )
                        }
                        
                        if (showFilterSheet) {
                            FilterBottomSheet(
                                availableGenres = currentState.availableGenres,
                                selectedGenres = currentState.selectedGenres,
                                onGenreSelect = onGenreSelect,
                                onClearFilters = onClearFilters,
                                onDismiss = { showFilterSheet = false },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A single movie item displayed in the grid.
 *
 * @param movie The movie data to display.
 * @param sharedTransitionScope The shared transition scope for poster transitions.
 * @param animatedVisibilityScope The animated visibility scope for poster transitions.
 * @param onClick Called when the item is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun MovieItem(
    movie: Movie,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column {
            with(sharedTransitionScope) {
                val bottomRadius by animatedVisibilityScope.transition.animateDp(label = "bottomRadius") { state ->
                    if (state == EnterExitState.Visible) 0.dp else 12.dp
                }

                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState(key = "poster-${movie.id.value}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .clip(
                            RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = bottomRadius,
                                bottomEnd = bottomRadius
                            )
                        )
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight,
                )
                Text(
                    text = movie.genres.joinToString { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Displayed when the movie list is empty due to active filters.
 *
 * @param onClearFilters Called when the user wants to clear all active filters.
 * @param modifier Component modifier.
 */
@Composable
private fun EmptyState(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.filter_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onClearFilters) {
            Text(stringResource(R.string.action_clear_filters))
        }
    }
}

/**
 * Displayed when an error occurs while fetching movies.
 *
 * @param message The error message to display.
 * @param onRetry Called when the user wants to retry the operation.
 * @param modifier Component modifier.
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun MovieItemPreview() {
    AmroTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                Surface(modifier = Modifier.padding(16.dp)) {
                    MovieItem(
                        movie = Movie(
                            id = MovieId("tmdb:1"),
                            title = "The Movie Title",
                            overview = "This is a brief overview of the movie.",
                            posterUrl = "",
                            genres = listOf(Genre(1, "Action"), Genre(2, "Drama")),
                            releaseDate = LocalDate(2024, 1, 1),
                            popularity = 8.5
                        ),
                        onClick = {},
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    AmroTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EmptyState(
                onClearFilters = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    AmroTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ErrorState(
                message = "Failed to load movies. Please check your connection.",
                onRetry = {},
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
