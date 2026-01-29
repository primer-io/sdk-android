package io.primer.sample.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.api.components.card.PrimerCardForm
import io.primer.android.api.components.card.CardFormDefaults
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.components.card.rememberCardFormController
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings

/**
 * Demo: Inline Card Form with Discount Code
 *
 * Layout:
 * - Row 1: Card number (full width)
 * - Row 2: Expiry | CVV | Cardholder name
 * - Row 3: Discount code input
 * - Row 4: Submit button
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun InlineCardFormDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val checkoutState by checkout.state.collectAsState()
    val isLoading = checkoutState is PrimerCheckoutState.Loading
    var discountCode by remember { mutableStateOf("") }

    PrimerCheckoutHost(checkout = checkout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Card Payment",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Custom inline layout with discount code",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                // Custom card form layout
                val cardFormController = rememberCardFormController(checkout)
                val cardState by cardFormController.state.collectAsStateWithLifecycle()
                PrimerCardForm(
                    controller = cardFormController,
                    cardDetails = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Row 1: Card number (full width)
                            CardFormDefaults.CardNumberField(
                                cardFormController = cardFormController,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            // Row 2: Expiry | CVV | Cardholder name
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(modifier = Modifier.weight(2f)) {
                                    CardFormDefaults.ExpiryField(
                                        cardFormController = cardFormController,
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    CardFormDefaults.CvvField(
                                        cardFormState = cardFormController,
                                    )
                                }
                                Box(modifier = Modifier.weight(1.5f)) {
                                    CardFormDefaults.CardholderField(
                                        cardFormController = cardFormController,
                                    )
                                }
                            }

                            // Row 3: Discount code
                            Text(
                                text = "Have a discount code?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )

                            OutlinedTextField(
                                value = discountCode,
                                onValueChange = { discountCode = it.uppercase() },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Discount Code") },
                                placeholder = { Text("Enter code") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                ),
                            )
                        }
                    },
                    submitButton = {
                        Button(
                            onClick = { cardFormController.submit() },
                            enabled = cardState.isFormValid && discountCode.isNotBlank()
                        ) {
                            Text("Submit")
                        }
                    },
                )
            }
        }
    }
}
