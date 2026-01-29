package io.primer.sample.demos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethods
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings

/**
 * Demo: Payment Method List Only (FlowSheet Mode)
 *
 * Shows how to use PaymentMethodList inline where selecting a card
 * automatically opens the CardForm in a FlowSheet.
 *
 * ## Two approaches for inline card form:
 *
 * ### 1. FlowSheet (this demo)
 * Use default item click behavior → CardForm opens in FlowSheet automatically.
 * Simplest integration, no state management needed.
 *
 * ### 2. Embedded (see InlineCheckoutDemo)
 * Don't use the default onClick for cards, instead:
 * - Set your own state: `selectedCard = true`
 * - Show `CardForm(checkout)` in your layout
 * This gives you full control over where the card form appears.
 *
 * Both approaches work - choose based on your UX needs.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun PaymentMethodListOnlyDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val paymentMethodState = rememberPaymentMethodsController(checkout)
    val checkoutState by checkout.state.collectAsState()
    val isLoading = checkoutState is PrimerCheckoutState.Loading
    PrimerCheckoutHost(checkout = checkout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "Select Payment Method",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap any method - card form opens in FlowSheet automatically",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                // Just the payment method list - nothing else needed!
                // When card is selected, CardForm opens in FlowSheet
                // When APM is selected, its native flow opens in FlowSheet
                PrimerPaymentMethods(controller = paymentMethodState)
            }
        }
    }
}
