package io.primer.components.clean.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.components.clean.model.PrimerPaymentMethod

@Composable
fun PrimerPaymentMethodItem(
    paymentMethod: PrimerPaymentMethod,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Text(
            text = paymentMethod.name,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
