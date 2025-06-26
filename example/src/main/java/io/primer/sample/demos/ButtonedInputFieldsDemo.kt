package io.primer.sample.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

// Level 5 - Advanced UI Patterns
object ButtonedInputFieldsDemo : CheckoutDemo(
    title = "Buttoned Input Fields",
    description = "Override all input fields with custom layout that open dialogs for a unique input experience",
    customizationLevel = 5,
    render = {

        // Card Details
        cardForm.cardNumberInput = {
            val state by cardForm.state.collectAsState()
            InputDialog(
                title = "Card Number",
                value = state.data[PrimerInputElementType.CARD_NUMBER] ?: "",
                onValueChange = { cardForm.updateCardNumber(it) },
            )
        }

        cardForm.expiryDateInput = {
            val state by cardForm.state.collectAsState()
            InputDialog(
                title = "Expiry Date",
                value = state.data[PrimerInputElementType.EXPIRY_DATE] ?: "",
                onValueChange = { cardForm.updateExpiryDate(it) },
                placeholder = "MM/YY"
            )
        }

        cardForm.cvvInput = {
            val state by cardForm.state.collectAsState()
            InputDialog(
                title = "CVV",
                value = state.data[PrimerInputElementType.CVV] ?: "",
                onValueChange = { cardForm.updateCvv(it) },
                placeholder = "123"
            )
        }

        cardForm.cardholderNameInput = {
            val state by cardForm.state.collectAsState()
            InputDialog(
                title = "Cardholder Name",
                value = state.data[PrimerInputElementType.CARDHOLDER_NAME] ?: "",
                onValueChange = { cardForm.updateCardholderName(it) },
            )
        }

        cardForm.screen = {

            val selectionState by paymentMethodSelection.state.collectAsState()
            if (selectionState is PrimerPaymentMethodSelectionScope.State.Ready) {
                cardForm.init()
            }

            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cardForm.cardNumberInput(Modifier)
                cardForm.expiryDateInput(Modifier)
                cardForm.cvvInput(Modifier)
                cardForm.cardholderNameInput(Modifier)
                cardForm.submitButton(Modifier, "Submit")
            }

        }
    }
)

@Composable
internal fun InputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    var tempValue by remember(value) { mutableStateOf(value) }
    var isShown by remember { mutableStateOf(false) }

    Button(
        onClick = {
            tempValue = value
            isShown = true
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (value.isNotEmpty()) "$title: $value" else title
        )
    }

    if (isShown) {
        Dialog(onDismissRequest = { isShown = false }) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = tempValue,
                        onValueChange = { tempValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        placeholder = if (placeholder.isNotEmpty()) {
                            { Text(placeholder) }
                        } else null
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            isShown = false
                        }) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = {
                                onValueChange(tempValue)
                                isShown = false
                            }
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

}
