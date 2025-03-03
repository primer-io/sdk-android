package io.primer.components.ui.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.components.Primer
import io.primer.components.models.PaymentMethod
import io.primer.components.models.render

@Composable
fun Primer.Scope.Checkout.RadioGroupExample() {
    val paymentMethods by paymentMethods.collectAsState()
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        paymentMethods.forEach { method ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMethod == method,
                    onClick = { selectedMethod = method }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(method.name)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedMethod?.render()
    }
}
