package com.amro.movies.features.movie.detail

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import com.amro.movies.R
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.Movie
import com.amro.movies.domain.model.MovieDetails
import com.amro.movies.domain.model.MovieId
import com.amro.movies.ui.navigation.Route
import com.amro.movies.ui.theme.AmroTheme
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.minutes

/**
 * Screen that displays comprehensive details for a specific movie.
 *
 * This screen fetches and displays movie information including title, backdrop, poster,
 * rating, genres, overview, and financial metadata. It supports shared element transitions
 * for the movie poster and integrates with a ViewModel to manage state and errors.
 *
 * @param movieId The unique identifier of the movie to display.
 * @param sharedTransitionScope The scope for coordinating shared element transitions.
 * @param animatedVisibilityScope The scope for managing visibility-based animations.
 * @param modifier Modifier to be applied to the screen's layout.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MovieDetailScreen(
    movieId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val viewModel: MovieDetailViewModel = assistedMetroViewModel(
        key = movieId,
        extras = MutableCreationExtras().apply {
            set(Route.MovieDetail.KEY_MOVIE_ID, movieId)
        },
    )
    
    val state by viewModel.state.collectAsStateWithLifecycle()

    MovieDetailScreenContent(
        state = state,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

/**
 * The stateless content of the Movie Detail screen.
 *
 * Separates the UI layout from the state management logic (ViewModel), facilitating
 * easier testing and previewing.
 *
 * @param state The current UI state to be rendered.
 * @param sharedTransitionScope Scope for shared element animations.
 * @param animatedVisibilityScope Scope for visibility animations.
 * @param onRetry Callback triggered when a retry is requested in an error state.
 * @param modifier Layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MovieDetailScreenContent(
    state: MovieDetailUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val showNavigationIcon = !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    if (showNavigationIcon) {
                        IconButton(
                            onClick = { backDispatcher?.onBackPressed() }
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back_24),
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .statusBarsPadding()
                    .zIndex(1f),
            )
        },
    ) { innerPadding ->
        Crossfade(targetState = state, label = "MovieDetailState") { currentState ->
            when (currentState) {
                is MovieDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.testTag("LoadingIndicator"))
                    }
                }
                is MovieDetailUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Text(
                                text = currentState.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                            )
                            Button(onClick = onRetry) {
                                Text(stringResource(R.string.action_retry))
                            }
                        }
                    }
                }
                is MovieDetailUiState.Success -> {
                    val details = currentState.details
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                    ) {
                        // Backdrop
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f),
                        ) {
                            details.backdropUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                                MaterialTheme.colorScheme.background,
                                            ),
                                        ),
                                    ),
                            )
                        }

                        // Content container overlapping the backdrop
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-64).dp)
                                .padding(horizontal = 16.dp)
                                .padding(bottom = innerPadding.calculateBottomPadding() + 32.dp),
                        ) {
                            // Title and Poster Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    MovieTitle(details)

                                    // Rating, Runtime, Status
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_star_24),
                                            contentDescription = null,
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.movie_details_rating_format, details.voteAverage),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        if (details.runtime != null) {
                                            Text(
                                                text = "  •  ${details.runtime}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline,
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(4.dp),
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = details.status,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                    }

                                    // Genres
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        details.movie.genres.take(3).forEach { genre ->
                                            Text(
                                                text = genre.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer.copy(
                                                            alpha = 0.4f
                                                        ),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Poster Image on the right
                                with(sharedTransitionScope) {
                                    val bottomRadius by animatedVisibilityScope.transition.animateDp(label = "bottomRadius") { state ->
                                        if (state == EnterExitState.Visible) 8.dp else 0.dp
                                    }

                                    AsyncImage(
                                        model = details.movie.posterUrl,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .sharedElement(
                                                rememberSharedContentState(key = "poster-${details.movie.id.value}"),
                                                animatedVisibilityScope = animatedVisibilityScope,
                                            )
                                            .width(120.dp)
                                            .aspectRatio(2f / 3f)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 8.dp,
                                                    topEnd = 8.dp,
                                                    bottomStart = bottomRadius,
                                                    bottomEnd = bottomRadius
                                                )
                                            )
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tagline
                            if (!details.tagline.isNullOrBlank()) {
                                Text(
                                    text = "\"${details.tagline}\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 16.dp),
                                )
                            }

                            // Overview
                            Text(
                                text = details.movie.overview,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 24.dp),
                            )

                            // Metadata Row (Popularity, Budget, Revenue)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                InfoColumn(
                                    label = stringResource(R.string.movie_details_popularity_label),
                                    value = stringResource(R.string.movie_details_popularity_format, details.movie.popularity),
                                )
                                InfoColumn(
                                    label = stringResource(R.string.movie_details_budget_label),
                                    value = if (details.budget <= 0) {
                                        stringResource(R.string.movie_details_not_available)
                                    } else {
                                        stringResource(R.string.movie_details_budget_revenue_format, details.budget)
                                    },
                                )
                                InfoColumn(
                                    label = stringResource(R.string.movie_details_revenue_label),
                                    value = if (details.revenue <= 0) {
                                        stringResource(R.string.movie_details_not_available)
                                    } else {
                                        stringResource(R.string.movie_details_budget_revenue_format, details.revenue)
                                    },
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // IMDB Link
                            if (details.imdbUrl != null) {
                                Button(
                                    onClick = {
                                        val intent = CustomTabsIntent.Builder().build()
                                        intent.launchUrl(context, details.imdbUrl.toUri())
                                    },
                                ) {
                                    Text(stringResource(R.string.movie_details_view_on_imdb))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the movie title and its release year in an annotated string.
 *
 * The title is emphasized with headline styling, while the release year is
 * displayed in parentheses with a more subtle appearance.
 *
 * @param details The movie details containing the title and release date.
 */
@Composable
private fun MovieTitle(details: MovieDetails) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.typography.headlineLarge.color,
                ),
            ) {
                append(details.movie.title)
            }
            if (details.movie.releaseDate != null) {
                append(" ")
                withStyle(
                    SpanStyle(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    append("(${details.movie.releaseDate.year})")
                }
            }
        },
        lineHeight = MaterialTheme.typography.headlineLarge.lineHeight,
        style = MaterialTheme.typography.headlineLarge.copy(
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Proportional,
                trim = LineHeightStyle.Trim.LastLineBottom,
            ),
        ),
        minLines = 2,
        maxLines = 3,
        overflow = TextOverflow.MiddleEllipsis,
    )
}

/**
 * Displays a labeled piece of information in a vertical column.
 *
 * Useful for showing metadata like budget, revenue, or popularity with a
 * consistent label-value hierarchy.
 *
 * @param label The descriptive label for the information.
 * @param value The actual data value to be displayed.
 */
@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieDetailLoadingPreview() {
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

@Preview(showBackground = true)
@Composable
private fun MovieDetailErrorPreview() {
    AmroTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                MovieDetailScreenContent(
                    state = MovieDetailUiState.Error("Failed to load movie details"),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onRetry = {}
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieDetailSuccessPreview() {
    AmroTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                MovieDetailScreenContent(
                    state = MovieDetailUiState.Success(
                        details = MovieDetails(
                            movie = Movie(
                                id = MovieId("tmdb:1"),
                                title = "The Matrix",
                                overview = "A computer hacker learns from mysterious rebels about the true nature of his reality.",
                                posterUrl = "https://image.tmdb.org/t/p/w500/f89U3Y9Yv98BMj9mZd46861Zp-v.jpg",
                                genres = listOf(Genre(1, "Action"), Genre(2, "Sci-Fi")),
                                releaseDate = LocalDate(1999, 3, 31),
                                popularity = 8.9
                            ),
                            tagline = "Welcome to the Real World",
                            backdropUrl = "https://image.tmdb.org/t/p/original/f89U3Y9Yv98BMj9mZd46861Zp-v.jpg",
                            voteAverage = 8.2,
                            voteCount = 20000,
                            budget = 63000000,
                            revenue = 463517383,
                            status = "Released",
                            imdbUrl = "https://www.imdb.com/title/tt0133093/",
                            runtime = 136.minutes
                        )
                    ),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this,
                    onRetry = {}
                )
            }
        }
    }
}
