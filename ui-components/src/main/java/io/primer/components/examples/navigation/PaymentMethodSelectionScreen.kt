package io.primer.components.examples.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.components.domain.PaymentFlowScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodSelectionScreen(
    scope: PaymentFlowScope,
    onMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top bar
        TopAppBar(
            title = { Text("Select Payment Method") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
        )

        // Payment methods list
        val methods by scope.paymentMethods.collectAsState()
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            items(methods) { method ->
                PaymentMethodItem(
                    method = method,
                    onClick = { onMethodSelected(method) },
                )
            }
        }
    }
}
