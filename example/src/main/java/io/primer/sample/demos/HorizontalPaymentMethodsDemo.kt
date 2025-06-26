package io.primer.sample.demos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

// Level 3 - Layout & Interaction Changes
object HorizontalPaymentMethodsDemo : CheckoutDemo(
    title = "Horizontal Payment Methods",
    description = "Override entire screen with horizontally scrolling payment methods featuring smooth animations",
    customizationLevel = 3,
    render = {
        paymentMethodSelection.screen = {

            val state by paymentMethodSelection.state.collectAsStateWithLifecycle()

            if (state is PrimerPaymentMethodSelectionScope.State.Ready) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Choose Your Payment Method",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Horizontal scrolling payment methods using actual data
                    val paymentMethods = (state as PrimerPaymentMethodSelectionScope.State.Ready).paymentMethods
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        paymentMethods.forEach { paymentMethod ->
                            Card(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(80.dp)
                                    .clickable {
                                        paymentMethodSelection.onPaymentMethodSelected(paymentMethod.paymentMethodType)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = when (paymentMethod.paymentMethodType) {
                                        "PAYMENT_CARD" -> Color(0xFF4CAF50)
                                        "PAYPAL" -> Color(0xFF0070BA)
                                        "GOOGLE_PAY" -> Color(0xFF4285F4)
                                        "KLARNA" -> Color(0xFFFFB3C7)
                                        else -> Color(0xFFF5F5F5)
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = paymentMethod.paymentMethodName
                                            ?: when (paymentMethod.paymentMethodType) {
                                                "PAYMENT_CARD" -> "Card"
                                                "PAYPAL" -> "PayPal"
                                                "GOOGLE_PAY" -> "Google Pay"
                                                "KLARNA" -> "Klarna"
                                                else -> paymentMethod.paymentMethodType
                                            },
                                        color = when (paymentMethod.paymentMethodType) {
                                            "PAYMENT_CARD", "PAYPAL", "GOOGLE_PAY" -> Color.White
                                            else -> Color.Black
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                LinearProgressIndicator(modifier = Modifier.padding(100.dp))
            }

        }
    }
)
