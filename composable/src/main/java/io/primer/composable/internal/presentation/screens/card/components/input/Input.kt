package io.primer.composable.internal.presentation.screens.card.components.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.scope.CardFormScope

@Composable
internal fun CardFormScope.Input(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
) {
    val state by state.collectAsState()

    // Check if this field should be shown
    val isFieldRequired = type in state.cardFields || type in state.billingFields
    if (!isFieldRequired) return

    val value = state.inputFields[type] ?: ""
    val error = state.fieldErrors.find { it.inputElementType == type }

    // Get configuration for this input type
    val config = remember(type, state.inputFields) {
        InputFieldConfigurations.getConfig(type, state.inputFields)
    }

    // Apply essential input filtering while letting validation framework provide feedback
    val onValueChange: (String) -> Unit = { newValue ->
        var processedValue = newValue

        // Apply allowed characters filter for strict input types (like CVV, card numbers)
        if (config.allowedChars != null) {
            processedValue = newValue.filter { it in config.allowedChars }
        }

        // Apply max length constraint to prevent excessive input
        if (config.maxLength != null && processedValue.length > config.maxLength) {
            processedValue = processedValue.take(config.maxLength)
        }

        updateInput(type to processedValue)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(config.label) },
        placeholder = { Text(config.placeholder) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = {
            error?.let {
                Text(
                    text = it.description,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        visualTransformation = config.visualTransformation,
        keyboardOptions = config.keyboardOptions,
    )
}
