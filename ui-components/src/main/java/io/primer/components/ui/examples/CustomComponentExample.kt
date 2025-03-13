package io.primer.components.ui.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.components.PrimerCheckoutScope
import io.primer.components.models.CardPaymentMethod
import io.primer.components.models.KlarnaPaymentMethod
import io.primer.components.ui.components.card.CardPaymentMethodScope

@Composable
fun PrimerCheckoutScope.CustomComponentExample() {
    val paymentMethods by paymentMethods.collectAsState()
    val selectedMethod = selectedPaymentMethod.collectAsState().value

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(paymentMethods) { method ->
            Button(onClick = {
                selectPaymentMethod(method)
            }) {
                Text(method.name.orEmpty())
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            when (selectedMethod) {
                is KlarnaPaymentMethod -> selectedMethod.DefaultContent()
                is CardPaymentMethod -> selectedMethod.Content(content = { CustomCardForm() })
                else -> Unit
            }
        }
    }
}

@Composable
fun CardPaymentMethodScope.CustomCardForm() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Some ugly card form from a merchant")

        HorizontalDivider()

        Button(onClick = { submit() }) {
            Text("Pay Now Card")
        }
    }
}
