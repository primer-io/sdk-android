package io.primer.android.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.primer.android.components.R
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.android.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.android.scope.PrimerCardFormScope

@Composable
internal fun PrimerCardFormScope.SubmitButton(
    modifier: Modifier = Modifier,
    text: String,
) {
    val currentState by state.collectAsState()

    PrimerButton(
        onClick = { onSubmit() },
        modifier = modifier.fillMaxWidth(),
        backgroundColor = LocalPrimerColorTokens.current.primerColorBrand,
        enabled = currentState.isSubmitEnabled,
    ) {
        Text(
            text = if (currentState.isLoading) stringResource(R.string.primer_components_checkout_loading) else text,
            style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle(),
            color = LocalPrimerColorTokens.current.primerColorBackground,
        )
    }
}
