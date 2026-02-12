package com.amro.movies.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.amro.movies.features.movie.detail.MovieDetailScreen
import com.amro.movies.features.movie.list.MovieListScreen
import com.amro.movies.ui.navigation.Route

/**
 * The main entry point for the AMRO Movies UI.
 *
 * This Composable sets up the application theme, top-level Scaffold, and
 * eventually coordinates navigation between different screens.
 *
 * @param modifier The modifier to be applied to the top-level layout.
 */
@Composable
fun MoviesApp(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {

        val backStack = rememberNavBackStack(Route.MovieList)

        NavDisplay(
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = entryProvider {
                entry<Route.MovieList> {
                    MovieListScreen(
                        modifier = Modifier.fillMaxSize(),
                        onMovieClick = { backStack.add(Route.MovieDetail) }
                    )
                }

                entry<Route.MovieDetail> {
                    MovieDetailScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        )
    }
}
