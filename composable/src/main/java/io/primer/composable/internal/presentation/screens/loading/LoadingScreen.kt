package io.primer.composable.internal.presentation.screens.loading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.theme.LocalPrimerSpacingTokens
import io.primer.composable.scope.PrimerCheckoutScope

@Composable
internal fun PrimerCheckoutScope.LoadingScreen(
    modifier: Modifier = Modifier,
    text: String = "Loading",
) {
    val spacing = LocalPrimerSpacingTokens.current
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = spacing.small),
        )
    }
}
