package io.primer.components.ui.examples

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.checkout.PrimerCheckoutScope
import io.primer.components.models.paymentMethods.PaymentMethod

@Composable
fun PrimerCheckoutScope.RadioGroupExample() {

    val state by state.collectAsStateWithLifecycle()

    var selectedPaymentMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    val readyState = state as? PrimerCheckoutScope.State.Ready
    val selectedState = state as? PrimerCheckoutScope.State.Selected

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        readyState?.paymentMethods?.let {
            items(it) { paymentMethod ->

                val isSelected = selectedPaymentMethod?.type == paymentMethod.type

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                selectedPaymentMethod = paymentMethod
                                selectPaymentMethod(paymentMethod)
                            },
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            selectedPaymentMethod = paymentMethod
                            selectPaymentMethod(paymentMethod)
                        },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(paymentMethod.name.orEmpty())
                }
            }
        }

        selectedState?.let {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Text(it.paymentMethod.name.orEmpty())
            }
        }
    }
}
