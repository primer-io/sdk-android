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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.composable.model.PrimerPaymentMethod

@Composable
internal fun PaymentMethodSelectionScreen(
    modifier: Modifier = Modifier,
    paymentMethods: List<PrimerPaymentMethod>,
    selectPaymentMethod: (PrimerPaymentMethod) -> Unit,
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = "Select Payment Method",
            style = MaterialTheme.typography.headlineSmall
        )

        HorizontalDivider(modifier = Modifier.padding(8.dp))

        // Payment Methods List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(paymentMethods) {
                PaymentMethodItem(
                    name = it.name
                ) {
                    selectPaymentMethod(it)
                }
            }
        }
    }
}

@Composable
internal fun PaymentMethodItem(
    modifier: Modifier = Modifier,
    name: String,
    onSelect: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = name,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
