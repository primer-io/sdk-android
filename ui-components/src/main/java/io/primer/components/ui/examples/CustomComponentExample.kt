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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.PrimerCheckoutScope
import io.primer.components.models.paymentMethods.CardPaymentMethod
import io.primer.components.ui.components.card.CardComponent
import io.primer.components.ui.components.card.CardPaymentMethodScope

@Composable
fun PrimerCheckoutScope.CustomComponentExample() {

    val state by state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        (state as? PrimerCheckoutScope.State.Ready?)?.paymentMethods?.let {
            items(it) { method ->
                Button(onClick = {
                    selectPaymentMethod(method)
                }) {
                    Text(method.name.orEmpty())
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            (state as? PrimerCheckoutScope.State.Selected?)?.paymentMethod?.let {
                when(val current = it) {
                    is CardPaymentMethod -> current.Render {
                        (this as CardPaymentMethodScope).CardComponent()
                    }
                }
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
