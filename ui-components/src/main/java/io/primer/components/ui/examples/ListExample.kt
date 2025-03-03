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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.components.Primer
import io.primer.components.models.render

@Composable
fun Primer.Scope.Checkout.ListExample() {
    val paymentMethods by paymentMethods.collectAsState()
    val selectedMethod by selectedPaymentMethod.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(paymentMethods) { method ->
            Button(onClick = {
                selectPaymentMethod(method)
            }) {
                Text(method.name)
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            selectedMethod?.render()
        }
    }
}
