package com.amro.movies.features.movie.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amro.movies.R

/**
 * Screen displaying the list of trending movies.
 *
 * This screen provides options to sort and filter movies, and allows users to
 * navigate to a detailed view of a selected movie.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(modifier: Modifier = Modifier, onMovieClick: () -> Unit) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.title_trending_movies))
                },
                actions = {
                    IconButton(onClick = { /* */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_sort_24),
                            contentDescription = stringResource(R.string.action_sort_movies)
                        )
                    }

                    IconButton(onClick = { /* */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_tune_24),
                            contentDescription = stringResource(R.string.action_filter_movies)
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text("Movie List")

            Button(
                onClick = { onMovieClick() }
            ) {
                Text("Go to Movie Detail")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieListScreenPreview() {
    MovieListScreen(onMovieClick = {})
}
