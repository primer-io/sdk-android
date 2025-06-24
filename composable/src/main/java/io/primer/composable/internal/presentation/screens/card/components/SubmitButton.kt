package io.primer.composable.internal.presentation.screens.card.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import io.primer.composable.internal.presentation.components.PrimerButton
import io.primer.composable.internal.presentation.theme.LocalPrimerColorTokens
import io.primer.composable.internal.presentation.theme.LocalPrimerTypographyTokens
import io.primer.composable.scope.PrimerCardFormScope

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
            text = if (currentState.isLoading) "Loading..." else text,
            style = LocalPrimerTypographyTokens.current.titleLarge.toTextStyle().copy(fontWeight = FontWeight(550)),
            color = LocalPrimerColorTokens.current.primerColorBackground,
        )
    }
}
