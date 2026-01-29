package io.primer.android.internal.presentation.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.scope.LocalCountrySelectionScope
import io.primer.android.scope.PrimerCountrySelectionScope

@Composable
internal fun PreviewContainer(
    darkTheme: Boolean = isSystemInDarkTheme(),
    countrySelectionState: PrimerCountrySelectionScope.State? = null,
    content: @Composable () -> Unit,
) {
    val countryScope = remember(countrySelectionState) {
        countrySelectionState?.let { PreviewMocks.createCountrySelectionScope(it) }
            ?: PreviewMocks.countrySelectionScope
    }
    CompositionLocalProvider(
        LocalPrimerTheme provides PrimerTheme(),
        LocalCountrySelectionScope provides countryScope,
    ) {
        PrimerTheme(darkTheme = darkTheme) {
            Surface(modifier = Modifier.padding(LocalPrimerTheme.current.spacingTokens.large)) {
                content()
            }
        }
    }
}
