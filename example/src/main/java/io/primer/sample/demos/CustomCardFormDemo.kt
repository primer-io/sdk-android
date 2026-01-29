package io.primer.sample.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.api.components.card.rememberCardFormController
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

/**
 * Demo: Fully Custom Card Form using rememberCardFormState()
 *
 * Shows how to build a completely custom card form UI using:
 * - rememberCardFormState() to get state + actions
 * - Standard Material3 OutlinedTextField instead of SDK fields
 * - Custom submit button with custom styling
 *
 * Key features demonstrated:
 * - Access card form state directly via cardFormState.state
 * - Update fields via cardFormState.updateCardNumber(), etc.
 * - Submit via cardFormState.onSubmit()
 * - Check validation via state.isFormValid
 * - Show loading via state.isLoading
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CustomCardFormDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val paymentMethodState = rememberPaymentMethodsController(checkout)

    val checkoutState by checkout.state.collectAsState()
    val isCheckoutLoading = checkoutState is PrimerCheckoutState.Loading
    val paymentMethods by paymentMethodState.paymentMethods.collectAsState()

    // Check if card payment method is available
    val hasCardMethod = paymentMethods.any {
        PaymentMethodType.safeValueOf(it.paymentMethodType) == PaymentMethodType.PAYMENT_CARD
    }

    PrimerCheckoutHost(checkout = checkout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {

            Text(
                text = "Custom Card Form",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Using rememberCardFormState() for fully custom UI",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isCheckoutLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                !hasCardMethod -> {
                    Text(
                        text = "Card payment method not available",
                        color = Color.Red,
                    )
                }

                else -> {
                    // Card Number
                    val cardFormState = rememberCardFormController(checkout)
                    val formState by cardFormState.state.collectAsState()
                    val cardNumber = formState.data[PrimerInputElementType.CARD_NUMBER].orEmpty()

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { value ->
                            // Filter to digits only and limit length
                            val filtered = value.filter { it.isDigit() }.take(19)
                            cardFormState.updateCardNumber(filtered)
                        },
                        label = { Text("Card Number") },
                        placeholder = { Text("4242 4242 4242 4242") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Expiry and CVV in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Expiry Date
                        val expiry = formState.data[PrimerInputElementType.EXPIRY_DATE].orEmpty()

                        OutlinedTextField(
                            value = expiry,
                            onValueChange = { value ->
                                // Format as MM/YY
                                val filtered = value.filter { it.isDigit() }.take(4)
                                val formatted = if (filtered.length > 2) {
                                    "${filtered.take(2)}/${filtered.drop(2)}"
                                } else {
                                    filtered
                                }
                                cardFormState.updateExpiryDate(formatted)
                            },
                            label = { Text("Expiry") },
                            placeholder = { Text("MM/YY") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )

                        // CVV
                        val cvv = formState.data[PrimerInputElementType.CVV].orEmpty()

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { value ->
                                val filtered = value.filter { it.isDigit() }.take(4)
                                cardFormState.updateCvv(filtered)
                            },
                            label = { Text("CVV") },
                            placeholder = { Text("123") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cardholder Name (if required)
                    if (PrimerInputElementType.CARDHOLDER_NAME in formState.cardFields) {
                        val cardholderName = formState.data[PrimerInputElementType.CARDHOLDER_NAME].orEmpty()

                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardFormState.updateCardholderName(it) },
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("John Doe") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Form validation status
                    Text(
                        text = if (formState.isFormValid) "Form is valid" else "Please complete all fields",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (formState.isFormValid) Color(0xFF4CAF50) else Color.Gray,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Custom submit button
                    Button(
                        onClick = { cardFormState.submit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = formState.isFormValid && !formState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2),
                            disabledContainerColor = Color(0xFFBDBDBD),
                        ),
                    ) {
                        if (formState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Pay Now",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
