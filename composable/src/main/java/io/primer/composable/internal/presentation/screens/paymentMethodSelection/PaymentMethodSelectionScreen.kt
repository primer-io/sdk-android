package io.primer.composable.internal.presentation.screens.paymentMethodSelection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodSelectionScreen(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()
    val paymentMethods =
        (state as? PaymentMethodSelectionScope.State.Ready)?.paymentMethods ?: emptyList()

    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.headlineSmall,
        )

        HorizontalDivider(modifier = Modifier.padding(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(paymentMethods) {
                PaymentMethodItem(
                    primerPaymentMethod = it,
                )
            }
        }
    }
}

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodItem(
    modifier: Modifier = Modifier,
    primerPaymentMethod: PrimerComposablePaymentMethod,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPaymentMethodSelected(primerPaymentMethod) },
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = primerPaymentMethod.paymentMethodName ?: "",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
