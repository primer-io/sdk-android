package io.primer.android.internal.presentation.screens.country.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.scope.LocalCountrySelectionScope

@Composable
internal fun DefaultSearchBar() {
    val scope = LocalCountrySelectionScope.current
    val state by scope.state.collectAsStateWithLifecycle()
    val spacing = LocalPrimerTheme.current.spacingTokens

    OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { scope.onSearch(it) },
        placeholder = { Text(stringResource(R.string.primer_country_placeholder_search)) },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_search),
                contentDescription = stringResource(R.string.accessibility_country_selection_search_icon),
            )
        },
        trailingIcon = {
            if (state.searchQuery.isNotEmpty()) {
                IconButton(onClick = { scope.onSearch("") }) {
                    Icon(
                        painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.accessibility_country_selection_clear),
                    )
                }
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.medium, vertical = spacing.small),
    )
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultSearchBarPreview() = PreviewContainer {
    DefaultSearchBar()
}
