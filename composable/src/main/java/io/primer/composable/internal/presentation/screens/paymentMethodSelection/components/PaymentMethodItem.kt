package io.primer.composable.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.primer.composable.internal.presentation.theme.LocalPrimerRadiusTokens
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodItem(
    modifier: Modifier = Modifier,
    borderColor: Color,
    backgroundColor: Color,
    onPaymentMethodSelected: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(LocalPrimerRadiusTokens.current.medium)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(LocalPrimerRadiusTokens.current.medium)
            )
            .clickable { onPaymentMethodSelected() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) { content() }
}
