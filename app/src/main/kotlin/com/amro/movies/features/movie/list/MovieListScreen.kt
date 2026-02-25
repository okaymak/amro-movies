package com.amro.movies.features.movie.list

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.amro.movies.R
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieId
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
 * @param modifier Screen modifier.
 * @param viewModel The view model managing the screen state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    onMovieClick: (Movie) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MovieListViewModel = metroViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    
    var showSortSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val successState = state as? MovieListUiState.Success
    
    // Auto-scroll to top when the movie list changes (e.g. after filtering/sorting)
    LaunchedEffect(successState?.movies) {
        if (successState != null) {
            gridState.scrollToItem(0)
        }
    }

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
                    IconButton(onClick = { showSortSheet = true }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_sort_24),
                            contentDescription = stringResource(R.string.action_sort_movies)
                        )
                    }

                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_filter_list_24px),
                            contentDescription = stringResource(R.string.action_filter_movies)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = appBarAlpha),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = appBarAlpha)
                )
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = (state as? MovieListUiState.Success)?.isRefreshing ?: false,
            onRefresh = viewModel::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (val currentState = state) {
                is MovieListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MovieListUiState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetry = viewModel::onRefresh,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MovieListUiState.Success -> {
                    if (currentState.movies.isEmpty()) {
                        EmptyState(
                            onClearFilters = viewModel::onClearFilters,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = innerPadding.calculateTopPadding() + 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(currentState.movies, key = { it.id.value }) { movie ->
                                MovieItem(movie = movie, onClick = { onMovieClick(movie) })
                            }
                        }
                    }

                    if (showSortSheet) {
                        SortBottomSheet(
                            currentField = currentState.currentSortField,
                            currentDirection = currentState.currentSortDirection,
                            onFieldSelect = viewModel::onSortFieldSelected,
                            onDirectionSelect = {
                                viewModel.onSortDirectionSelected(it)
                                showSortSheet = false
                            },
                            onDismiss = { showSortSheet = false }
                        )
                    }
                    
                    if (showFilterSheet) {
                        FilterBottomSheet(
                            availableGenres = currentState.availableGenres,
                            selectedGenres = currentState.selectedGenres,
                            onGenreSelect = viewModel::onGenreSelected,
                            onClearFilters = viewModel::onClearFilters,
                            onDismiss = { showFilterSheet = false }
                        )
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
 * @param onClick Called when the item is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieItem(
    movie: Movie,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.titleMedium.lineHeight
                )
                Text(
                    text = movie.genres.joinToString { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = stringResource(R.string.filter_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
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
                onClick = {}
            )
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
                modifier = Modifier.fillMaxSize()
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
