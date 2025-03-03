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
import io.primer.components.Primer
import io.primer.components.models.PaymentMethod
import io.primer.components.models.render

@Composable
fun Primer.Scope.Checkout.CustomComponentExample() {

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
            when(selectedMethod?.type) {
                PaymentMethod.Type.CARD -> CustomCardForm(scope = this@CustomComponentExample)
                PaymentMethod.Type.GOOGLE_PAY -> selectedMethod?.render()
                PaymentMethod.Type.KLARNA -> CustomKlarna(scope = this@CustomComponentExample)
                null -> Unit
            }
        }
    }
}


@Composable
fun CustomKlarna(scope: Primer.Scope.Checkout) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Another example of custom component for klarna")

        Button(onClick = { scope.pay() }) {
            Text("Pay Now Klarna")
        }
    }
}

@Composable
fun CustomCardForm(scope: Primer.Scope.Checkout) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Some ugly card form from a merchant")
        
        HorizontalDivider()
        
        Button(onClick = { scope.pay() }) {
            Text("Pay Now Card")
        }
    }
}
