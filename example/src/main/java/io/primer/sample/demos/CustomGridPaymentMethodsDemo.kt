package io.primer.sample.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

/**
 * Demo: Custom Grid Payment Methods
 *
 * Shows how to use rememberPaymentMethodState() to build completely custom UI.
 * Uses a 2-column grid layout instead of the default vertical list.
 *
 * Key features:
 * - Uses rememberPaymentMethodState() to get state + actions bundled
 * - Builds custom grid layout (not using PaymentMethodList at all)
 * - state.select() still works the same - opens FlowSheet for cards/APMs
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CustomGridPaymentMethodsDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val paymentMethodState = rememberPaymentMethodsController(checkout)
    val paymentMethods by paymentMethodState.paymentMethods.collectAsState()
    val checkoutState by checkout.state.collectAsState()
    val isLoading = checkoutState is PrimerCheckoutState.Loading
    PrimerCheckoutHost(checkout = checkout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "Choose Payment Method",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Using rememberPaymentMethodState() for custom grid UI",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Custom 2-column grid - NOT using PaymentMethodList!
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(paymentMethods) { method ->
                        PaymentMethodGridItem(
                            method = method,
                            onClick = { paymentMethodState.select(method) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodGridItem(
    method: PrimerComposablePaymentMethod,
    onClick: () -> Unit,
) {
    val type = PaymentMethodType.safeValueOf(method.paymentMethodType)
    val backgroundColor = when (type) {
        PaymentMethodType.PAYMENT_CARD -> Color(0xFFE3F2FD)
        PaymentMethodType.PAYPAL -> Color(0xFFFFF8E1)
        PaymentMethodType.GOOGLE_PAY -> Color(0xFFE8F5E9)
        PaymentMethodType.KLARNA -> Color(0xFFFCE4EC)
        else -> Color(0xFFF5F5F5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Payment method icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = method.paymentMethodType.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment method name
            Text(
                text = method.paymentMethodName ?: method.paymentMethodType,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}
