package com.amro.movies.features.movie.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amro.movies.R
import com.amro.movies.domain.model.Genre
import com.amro.movies.domain.model.SortDirection
import com.amro.movies.domain.model.SortField
import com.amro.movies.ui.theme.AmroTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/**
 * A modal bottom sheet for selecting movie sorting criteria.
 *
 * @param currentField The currently selected sort field.
 * @param currentDirection The currently selected sort direction.
 * @param onFieldSelect Callback invoked when a sort field is selected.
 * @param onDirectionSelect Callback invoked when a sort direction is selected.
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 */
@Suppress("ModifierRequired")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentField: SortField,
    currentDirection: SortDirection,
    onFieldSelect: (SortField) -> Unit,
    onDirectionSelect: (SortDirection) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        SortBottomSheetContent(
            currentField = currentField,
            currentDirection = currentDirection,
            onFieldSelect = onFieldSelect,
            onDirectionSelect = onDirectionSelect
        )
    }
}

/**
 * The content of the [SortBottomSheet]. This composable displays options for
 * sorting by different fields and directions.
 *
 * @param currentField The currently selected sort field.
 * @param currentDirection The currently selected sort direction.
 * @param onFieldSelect Callback invoked when a sort field is selected.
 * @param onDirectionSelect Callback invoked when a sort direction is selected.
 */
@Suppress("ModifierRequired")
@Composable
fun SortBottomSheetContent(
    currentField: SortField,
    currentDirection: SortDirection,
    onFieldSelect: (SortField) -> Unit,
    onDirectionSelect: (SortDirection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.sort_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
        )

        SortField.entries.forEach { field ->
            val isSelected = field == currentField
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { onFieldSelect(field) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null
                )
                Text(
                    text = when (field) {
                        SortField.TITLE -> stringResource(R.string.sort_field_title)
                        SortField.RELEASE_DATE -> stringResource(R.string.sort_field_release_date)
                        SortField.POPULARITY -> stringResource(R.string.sort_field_popularity)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp).weight(1f)
                )
                
                val ascendingIconColor = when {
                    isSelected && currentDirection == SortDirection.ASCENDING -> MaterialTheme.colorScheme.primary
                    isSelected -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    else -> Color.Transparent
                }
                val descendingIconColor = when {
                    isSelected && currentDirection == SortDirection.DESCENDING -> MaterialTheme.colorScheme.primary
                    isSelected -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    else -> Color.Transparent
                }

                Row {
                    IconButton(
                        onClick = { onDirectionSelect(SortDirection.ASCENDING) },
                        enabled = isSelected
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_upward_24),
                            contentDescription = stringResource(R.string.sort_direction_ascending),
                            tint = ascendingIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDirectionSelect(SortDirection.DESCENDING) },
                        enabled = isSelected
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_downward_24),
                            contentDescription = stringResource(R.string.sort_direction_descending),
                            tint = descendingIconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A modal bottom sheet for filtering movies by genre.
 *
 * @param availableGenres The list of all available genres to choose from.
 * @param selectedGenres The set of currently selected genre IDs.
 * @param onGenreSelect Callback invoked when a genre is selected or deselected.
 * @param onClearFilters Callback invoked when the user chooses to clear all filters.
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 */
@Suppress("ModifierRequired")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    availableGenres: ImmutableList<Genre>,
    selectedGenres: ImmutableSet<Int>,
    onGenreSelect: (Int) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        FilterBottomSheetContent(
            availableGenres = availableGenres,
            selectedGenres = selectedGenres,
            onGenreSelect = onGenreSelect,
            onClearFilters = onClearFilters
        )
    }
}

/**
 * The content of the [FilterBottomSheet]. This composable displays a list of
 * available genres as filter chips.
 *
 * @param availableGenres The list of all available genres to choose from.
 * @param selectedGenres The set of currently selected genre IDs.
 * @param onGenreSelect Callback invoked when a genre is selected or deselected.
 * @param onClearFilters Callback invoked when the user chooses to clear all filters.
 */
@Suppress("ModifierRequired")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheetContent(
    availableGenres: ImmutableList<Genre>,
    selectedGenres: ImmutableSet<Int>,
    onGenreSelect: (Int) -> Unit,
    onClearFilters: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.filter_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClearFilters) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_filter_list_off_24px),
                    contentDescription = stringResource(R.string.action_clear_filters)
                )
            }
        }
        
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableGenres.forEach { genre ->
                FilterChip(
                    selected = genre.id in selectedGenres,
                    onClick = { onGenreSelect(genre.id) },
                    label = { Text(genre.name) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SortBottomSheetPreview() {
    AmroTheme {
        Surface {
            SortBottomSheetContent(
                currentField = SortField.POPULARITY,
                currentDirection = SortDirection.DESCENDING,
                onFieldSelect = {},
                onDirectionSelect = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterBottomSheetPreview() {
    AmroTheme {
        Surface {
            FilterBottomSheetContent(
                availableGenres = persistentListOf(
                    Genre(1, "Action"),
                    Genre(2, "Comedy"),
                    Genre(3, "Drama"),
                    Genre(4, "Sci-Fi"),
                    Genre(5, "Thriller")
                ),
                selectedGenres = persistentSetOf(1, 3),
                onGenreSelect = {},
                onClearFilters = {}
            )
        }
    }
}
