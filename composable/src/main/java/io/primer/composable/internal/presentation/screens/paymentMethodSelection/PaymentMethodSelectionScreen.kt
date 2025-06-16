package io.primer.composable.internal.presentation.screens.paymentMethodSelection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.composable.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.composable.internal.presentation.components.SurchargeLabel
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodSelectionScreen(
    modifier: Modifier = Modifier,
) {
    val state by state.collectAsStateWithLifecycle()
    val readyState = state as? PaymentMethodSelectionScope.State.Ready
    val paymentMethods = readyState?.paymentMethods ?: emptyList()
    val currency = readyState?.currency

    Column(modifier = modifier.padding(8.dp)) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(paymentMethods) {
                PaymentMethodItem(
                    primerPaymentMethod = it,
                    currency = currency,
                )
            }
        }
    }
}

@Composable
internal fun PaymentMethodSelectionScope.PaymentMethodItem(
    modifier: Modifier = Modifier,
    primerPaymentMethod: PrimerComposablePaymentMethod,
    currency: java.util.Currency? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPaymentMethodSelected(primerPaymentMethod) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = primerPaymentMethod.paymentMethodName ?: "",
                style = MaterialTheme.typography.titleMedium,
            )
            
            SurchargeLabel(
                surcharge = primerPaymentMethod.surcharge,
                currency = currency,
            )
        }
    }
}
