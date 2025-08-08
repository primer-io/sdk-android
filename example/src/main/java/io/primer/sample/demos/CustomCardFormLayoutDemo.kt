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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

object CustomCardFormLayoutDemo : CheckoutDemo(
    title = "Custom Card Form Layout",
    description = "Override individual components with custom styling while maintaining functionality",
    customizationLevel = 4,
    render = {
        // Override individual input components with custom styling

        components.cardForm.screen = {

            var selectedLayout by remember { mutableStateOf("Column") }
            val layoutOptions = listOf("Column", "Row", "Grid 2x2")

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
                            with(components) {
                                cardNumberInput(Modifier)
                                expiryDateInput(Modifier)
                                cvvInput(Modifier)
                                cardholderNameInput(Modifier)
                                submitButton(Modifier, "Submit")
                            }
                        }
                    }

                    "Row" -> {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            with(components) {
                                cardNumberInput(Modifier
                                    .width(200.dp)
                                    .height(50.dp))
                                expiryDateInput(Modifier
                                    .width(200.dp)
                                    .height(50.dp))
                                cvvInput(Modifier
                                    .width(200.dp)
                                    .height(50.dp))
                                cardholderNameInput(Modifier
                                    .width(200.dp)
                                    .height(50.dp))
                                submitButton(Modifier
                                    .width(200.dp)
                                    .height(50.dp), "Submit")
                            }
                        }
                    }

                    "Grid 2x2" -> {
                        Column {
                            with(components) {
                                Row {
                                    cardNumberInput(Modifier.weight(1f))
                                    expiryDateInput(Modifier.weight(1f))
                                }
                                Row {
                                    cvvInput(Modifier.weight(1f))
                                    cardholderNameInput(Modifier.weight(1f))
                                }
                                submitButton(Modifier.fillMaxWidth(), "Submit")
                            }
                        }
                    }
                }
            }
        }
    }
)
