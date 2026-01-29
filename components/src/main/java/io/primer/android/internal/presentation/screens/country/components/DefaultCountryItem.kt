package io.primer.android.internal.presentation.screens.country.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.clientSessionActions.domain.models.PrimerCountry
import io.primer.android.components.R
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.configuration.data.model.emojiFlag
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.scope.LocalCountrySelectionScope

typealias CountryItemComponent = @Composable (country: PrimerCountry) -> Unit

@Composable
internal fun DefaultCountryItem(
    country: PrimerCountry,
) {
    val spacing = LocalPrimerTheme.current.spacingTokens
    val scope = LocalCountrySelectionScope.current
    val colorTokens = LocalPrimerTheme.current.colorTokens()
    val accessibilityLabel = stringResource(R.string.accessibility_country_selection_item, country.name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { scope.onCountrySelected(country.code.name, country.name) }
            .semantics { contentDescription = accessibilityLabel }
            .padding(horizontal = spacing.large, vertical = spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = country.code.emojiFlag(),
            modifier = Modifier.height(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = country.name,
            style = MaterialTheme.typography.bodyLarge,
            color = colorTokens.primerColorTextPrimary,
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultCountryItemPreview() = PreviewContainer {
    DefaultCountryItem(PrimerCountry("United States", CountryCode.US))
}
