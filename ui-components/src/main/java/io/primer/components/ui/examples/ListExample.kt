package io.primer.components.ui.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.checkout.PrimerCheckoutScope

@Composable
fun PrimerCheckoutScope.ListExample() {

    val state by state.collectAsStateWithLifecycle()

    val readyState = state as? PrimerCheckoutScope.State.Ready
    val selectedState = state as? PrimerCheckoutScope.State.Selected

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        readyState?.paymentMethods?.let {
            items(it) { method ->
                Button(onClick = { selectPaymentMethod(method) }) {
                    Text(method.name.orEmpty())
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        selectedState?.paymentMethod?.let {
            item {
                it.Default()
            }
        }
    }
}
