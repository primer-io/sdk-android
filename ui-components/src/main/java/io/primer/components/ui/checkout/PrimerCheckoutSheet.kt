package io.primer.components.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.components.PrimerCheckoutScope
import io.primer.components.models.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimerCheckoutScope.PrimerCheckoutSheet(modifier: Modifier = Modifier) {
    val paymentMethods by paymentMethods.collectAsState()
    val selectedPaymentMethod by selectedPaymentMethod.collectAsState()

    ModalBottomSheet(
        onDismissRequest = {}, // TODO TWS: implement dismiss
        sheetState = rememberModalBottomSheetState(),
        modifier = modifier.fillMaxWidth().padding(16.dp), // TODO TWS: use dimens from theme
    ) {
        selectedPaymentMethod?.let { paymentMethod ->
            SelectedPaymentMethod(paymentMethod, onBackClick = { selectPaymentMethod(null) })
        } ?: PaymentMethodList(paymentMethods)
    }
}

@Composable
private fun SelectedPaymentMethod(paymentMethod: PaymentMethod<*>, onBackClick: () -> Unit) {
    Column {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null) // TODO TWS: set content description
        }

        paymentMethod.DefaultContent()
    }
}

@Composable
private fun PrimerCheckoutScope.PaymentMethodList(paymentMethods: List<PaymentMethod<*>>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(paymentMethods) { paymentMethod ->
            Button(
                onClick = { selectPaymentMethod(paymentMethod) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(paymentMethod.name.orEmpty())
            }
        }
    }
}
