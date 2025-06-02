package io.primer.composable.internal.presentation.screens.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.primer.composable.internal.presentation.screens.card.components.BillingAddressForm
import io.primer.composable.internal.presentation.screens.card.components.CardDetailsForm
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.CardFormScreen(
    modifier: Modifier = Modifier,
    submitButton: (@Composable () -> Unit)? = { SubmitButton(text = "Submit") }
) {
    val state: CardFormScope.State by state.collectAsState()

    when (val current = state) {
        is CardFormScope.State.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is CardFormScope.State.Ready -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (current.cardInputFields.isNotEmpty()) {
                    Text(
                        text = "CARD DETAILS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    CardDetailsForm(
                        cardInputFields = current.cardInputFields
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (current.billingInputFields.isNotEmpty()) {
                    Text(
                        text = "BILLING ADDRESS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    BillingAddressForm(
                        billingInputFields = current.billingInputFields
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                submitButton?.invoke()
            }
        }
    }
}

@Composable
internal fun CardFormScope.SubmitButton(
    modifier: Modifier = Modifier,
    text: String
) {
    Button(
        onClick = { submit() },
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
