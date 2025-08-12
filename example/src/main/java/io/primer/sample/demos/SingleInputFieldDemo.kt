package io.primer.sample.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

object SingleInputFieldDemo : CheckoutDemo(
    title = "Single Input Field View",
    description = "Override screen to display one input field at a time with Previous/Next navigation controls",
    customizationLevel = 4,
    render = {

        components.cardForm.submitButton = { modifier, text ->
            IconButton(
                // TODO somehow it's not triggering submit because it's stuck at isSubmitAllowed rawDataManagerRepository.validationState.first()
                onClick = { onSubmit() }
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        components.cardForm.screen = {

            val cardFormState by state.collectAsStateWithLifecycle()

            if (cardFormState.cardFields.isNotEmpty()) {
                var currentFieldIndex by remember { mutableIntStateOf(0) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Step ${currentFieldIndex + 1} of ${cardFormState.cardFields.size}",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    with(components) {
                        when (cardFormState.cardFields[currentFieldIndex]) {
                            PrimerInputElementType.CARD_NUMBER -> cardNumberInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.CVV -> cvvInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.EXPIRY_DATE -> expiryDateInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.CARDHOLDER_NAME -> cardholderNameInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.POSTAL_CODE -> postalCodeInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.COUNTRY_CODE -> countryCodeInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.CITY -> cityInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.STATE -> stateInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.ADDRESS_LINE_1 -> addressLine1Input(Modifier.fillMaxWidth())
                            PrimerInputElementType.ADDRESS_LINE_2 -> addressLine2Input(Modifier.fillMaxWidth())
                            PrimerInputElementType.PHONE_NUMBER -> phoneNumberInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.FIRST_NAME -> firstNameInput(Modifier.fillMaxWidth())
                            PrimerInputElementType.LAST_NAME -> lastNameInput(Modifier.fillMaxWidth())
                            else -> Unit
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentFieldIndex > 0) currentFieldIndex--
                            },
                            enabled = currentFieldIndex > 0
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                tint = if (currentFieldIndex > 0)
                                    MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            cardFormState.cardFields.indices.forEach { index ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentFieldIndex)
                                                MaterialTheme.colorScheme.primary
                                            else Color.LightGray
                                        )
                                )
                            }
                        }

                        if (currentFieldIndex < cardFormState.cardFields.size - 1) {
                            IconButton(
                                onClick = { currentFieldIndex++ }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            with(components) {
                                submitButton(Modifier, "Submit")
                            }
                        }
                    }
                }
            }
        }
    }
)
