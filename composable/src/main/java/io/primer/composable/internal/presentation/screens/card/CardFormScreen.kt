package io.primer.composable.internal.presentation.screens.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.CardFormScope.Companion.PrimerBillingAddress
import io.primer.composable.scope.CardFormScope.Companion.PrimerCardDetails
import io.primer.composable.scope.CardFormScope.Companion.PrimerSubmitButton

@Composable
internal fun CardFormScope.CardFormScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        PrimerCardDetails()
        PrimerBillingAddress()
        PrimerSubmitButton()
    }
}
