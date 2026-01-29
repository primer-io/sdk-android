package io.primer.android.internal.presentation.screens.country

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.primer.android.LocalPrimerTheme
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.R
import io.primer.android.internal.extensions.toFlagEmoji

/**
 * Internal country selection screen component.
 * Handles country selection.
 */
@Composable
internal fun CountrySelectionScreen(
    navController: NavController,
) {
    val viewModel = viewModel<CountrySelectionViewModel>(
        factory = CountrySelectionViewModelFactory(),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalPrimerTheme.current.spacingTokens

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onSearch(it) },
            placeholder = { Text(stringResource(R.string.primer_country_placeholder_search)) },
            leadingIcon = {
                Icon(
                    painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.primer_country_placeholder_search),
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearch("") }) {
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
                .padding(horizontal = spacing.medium, vertical = spacing.xsmall),
        )

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium),
            ) {
                items(state.filteredCountries) { country ->
                    CountryItem(
                        country = country,
                        onClick = {
                            viewModel.onCountrySelected(
                                countryName = country.name,
                                countryCode = country.code.name,
                            )
                            navController.popBackStack()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryItem(
    country: PrimerCountry,
    onClick: () -> Unit,
) {
    val spacing = LocalPrimerTheme.current.spacingTokens
    val colorTokens = LocalPrimerTheme.current.colorTokens()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = spacing.medium)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            Text(
                text = country.code.name.toFlagEmoji(),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = colorTokens.primerColorTextPrimary,
            )
        }
    }
}
