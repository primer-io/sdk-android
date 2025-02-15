package io.primer.components.ui.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.components.domain.PaymentFlowScope
import io.primer.components.domain.models.PaymentResult
import kotlinx.coroutines.launch

@Composable
internal fun DefaultCheckoutContent(
    modifier: Modifier = Modifier,
    scope: PaymentFlowScope,
    onPaymentCompleted: (PaymentResult) -> Unit,
) {
    val selectedMethod by scope.selectedMethod.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier.fillMaxWidth()) {
        when (selectedMethod) {
            null -> {
                val methods by scope.paymentMethods.collectAsState()
                LazyColumn {
                    items(methods) { method ->
                        ListItem(
                            headlineContent = { Text(method.paymentMethodName.orEmpty()) },
                            modifier = Modifier.clickable {
                                scope.selectPaymentMethod(method)
                            },
                        )
                    }
                }
            }

            else -> {
                scope.PaymentMethodContent(selectedMethod!!) {
                    Column {
                        IconButton(onClick = { scope.selectPaymentMethod(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }

                        val methodState by state.collectAsStateWithLifecycle()
                        DefaultContent()
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    submit().onSuccess { result ->
                                        onPaymentCompleted(result)
                                    }
                                }
                            },
                            enabled = methodState.validationState.isValid,
                        ) {
                            Text("Pay with ${method.paymentMethodName.orEmpty()}") // TODO TWS: use string resources
                        }
                    }
                }
            }
        }
    }
}
