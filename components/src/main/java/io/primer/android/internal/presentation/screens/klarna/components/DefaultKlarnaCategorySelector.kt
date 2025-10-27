package io.primer.android.internal.presentation.screens.klarna.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.scope.PrimerKlarnaScope

@Composable
internal fun PrimerKlarnaScope.DefaultKlarnaCategorySelector(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxWidth().padding(LocalPrimerTheme.current.spacingTokens.large)) {
        state.categories.forEach { category ->
            Button(
                onClick = { selectPaymentCategory(category.id) },
                modifier = Modifier.padding(vertical = LocalPrimerTheme.current.spacingTokens.xsmall),
            ) {
                Text(if (category.id == state.selectedCategoryId) "✓ ${category.name}" else category.name)
            }
        }
    }
}
