package io.primer.sample.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

object CustomCardComponentDemo : CheckoutDemo(
    title = "Custom Card Component",
    description = "Replace payment method cards with progress bars that auto-complete and trigger selection",
    customizationLevel = 2,
    render = {
        paymentMethodSelection.paymentMethodCard = { modifier ->
            var progress by remember { mutableFloatStateOf(0f) }

            Card(
                modifier = modifier
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Card Payment",
                            fontWeight = FontWeight.Medium
                        )
                        if (progress >= 1f) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }
                    Slider(
                        value = progress,
                        onValueChange = { progress = it }
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% - ${if (progress < 1f) "Loading..." else "Ready!"}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (progress >= 1f) {
                paymentMethodSelection.onPaymentMethodSelected(PaymentMethodType.PAYMENT_CARD.name)
            }
        }
    }
)
