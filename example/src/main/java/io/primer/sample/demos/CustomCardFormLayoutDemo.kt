package io.primer.sample.demos

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

object CustomCardFormLayoutDemo : CheckoutDemo(
    title = "Custom Card Form Layout",
    description = "Override individual components with custom styling while maintaining functionality",
    customizationLevel = 4,
    render = {
        // Override individual input components with custom styling
        cardForm.screen = {

            val selectionState by paymentMethodSelection.state.collectAsState()
            var selectedLayout by remember { mutableStateOf("Column") }
            val layoutOptions = listOf("Column", "Row", "Grid 2x2")

            if (selectionState is PrimerPaymentMethodSelectionScope.State.Ready) {
                cardForm.init()
            }

            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Layout Selection RadioGroup
                Text(
                    text = "Layout Options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(modifier = Modifier.selectableGroup()) {
                    layoutOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (option == selectedLayout),
                                    onClick = { selectedLayout = option },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == selectedLayout),
                                onClick = null
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }

                // Dynamic Layout based on selection
                when (selectedLayout) {
                    "Column" -> {
                        Column {
                            cardForm.cardNumberInput(Modifier)
                            cardForm.expiryDateInput(Modifier)
                            cardForm.cvvInput(Modifier)
                            cardForm.cardholderNameInput(Modifier)
                            cardForm.submitButton(Modifier, "Submit")
                        }
                    }
                    "Row" -> {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            cardForm.cardNumberInput(Modifier.width(200.dp).height(50.dp))
                            cardForm.expiryDateInput(Modifier.width(200.dp).height(50.dp))
                            cardForm.cvvInput(Modifier.width(200.dp).height(50.dp))
                            cardForm.cardholderNameInput(Modifier.width(200.dp).height(50.dp))
                            cardForm.submitButton(Modifier.width(200.dp).height(50.dp), "Submit")
                        }
                    }
                    "Grid 2x2" -> {
                        Column {
                            Row {
                                cardForm.cardNumberInput(Modifier.weight(1f))
                                cardForm.expiryDateInput(Modifier.weight(1f))
                            }
                            Row {
                                cardForm.cvvInput(Modifier.weight(1f))
                                cardForm.cardholderNameInput(Modifier.weight(1f))
                            }
                            cardForm.submitButton(Modifier.fillMaxWidth(), "Submit")
                        }
                    }
                }
            }
        }
    }
)
