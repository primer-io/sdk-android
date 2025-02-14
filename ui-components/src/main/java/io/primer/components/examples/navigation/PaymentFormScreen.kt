package io.primer.components.examples.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.components.domain.PaymentFlowScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFormScreen(
    scope: PaymentFlowScope,
    method: PrimerHeadlessUniversalCheckoutPaymentMethod,
    onBackClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = { Text("Enter Card Details") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
        )

        scope.PaymentMethodContent(method) {
            val coroutine = rememberCoroutineScope()
            Column(modifier = Modifier.padding(16.dp)) {
                // Payment form
                DefaultContent()

                Spacer(Modifier.height(16.dp))

                // Pay button
                val contentState by state.collectAsState()
                if (contentState.isLoading.not()) {
                    Button(
                        onClick = {
                            coroutine.launch { submit() }
                        },
                        enabled = contentState.validationState.isValid,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Pay with ${method.paymentMethodName.orEmpty()}")
                    }
                }
            }
        }
    }
}
