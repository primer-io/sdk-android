package io.primer.android.internal.presentation.screens.country.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.primer.android.LocalPrimerTheme
import io.primer.android.clientSessionActions.domain.models.PrimerCountry

@Composable
internal fun DefaultCountryItem(
    country: PrimerCountry,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalPrimerTheme.current.spacingTokens
    val colorTokens = LocalPrimerTheme.current.colorTokens()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.medium),
            text = country.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = colorTokens.primerColorTextPrimary,
        )
        HorizontalDivider()
    }
}
