package com.amro.movies.ui

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.amro.movies.features.movie.detail.MovieDetailScreen
import com.amro.movies.features.movie.list.MovieListScreen
import com.amro.movies.ui.navigation.Route

/**
 * The main entry point for the AMRO Movies UI.
 *
 * This Composable sets up the application theme, top-level Scaffold, and
 * coordinates navigation between different screens with shared element transitions.
 *
 * @param modifier The modifier to be applied to the top-level layout.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MoviesApp(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val backStack = rememberNavBackStack(Route.MovieList)
        val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()

        SharedTransitionLayout {
            NavDisplay(
                sceneStrategy = sceneStrategy,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                backStack = backStack,
                onBack = {
                    backStack.removeLastOrNull()
                },
                entryProvider = entryProvider {
                    entry<Route.MovieList>(
                        metadata = ListDetailSceneStrategy.listPane()
                    ) {
                        MovieListScreen(
                            modifier = Modifier.fillMaxSize(),
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            onMovieClick = { movie ->
                                if (backStack.lastOrNull() is Route.MovieDetail) {
                                    backStack.removeAt(backStack.size - 1)
                                }
                                backStack.add(Route.MovieDetail(movie.id.value))
                            }
                        )
                    }

                    entry<Route.MovieDetail>(
                        metadata = ListDetailSceneStrategy.detailPane()
                    ) { route ->
                        MovieDetailScreen(
                            movieId = route.movieId,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            )
        }
    }
}
