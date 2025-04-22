package io.primer.components.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.checkout.PrimerCheckoutScope
import io.primer.components.models.paymentMethods.PaymentMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimerCheckoutScope.PrimerCheckoutSheet(modifier: Modifier = Modifier) {
    val state by state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = {}, // TODO TWS: implement dismiss
        sheetState = rememberModalBottomSheetState(),
        dragHandle = {},
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp) // TODO TWS: use dimens from theme
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .heightIn(min = 300.dp)
        ) {
            when (val currentState = state) {
                PrimerCheckoutScope.State.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PrimerCheckoutScope.State.Ready -> {
                    PaymentMethodList(currentState.paymentMethods)
                }

                is PrimerCheckoutScope.State.Selected -> {
                    SelectedPaymentMethod(
                        currentState.paymentMethod,
                        onBackClick = { clearSelectedPaymentMethod() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedPaymentMethod(paymentMethod: PaymentMethod, onBackClick: () -> Unit) {
    Column {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null) // TODO TWS: set content description
        }
        paymentMethod.Render()
    }
}

@Composable
private fun PrimerCheckoutScope.PaymentMethodList(paymentMethods: List<PaymentMethod>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
