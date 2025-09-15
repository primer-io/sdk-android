package io.primer.android.internal.presentation.screens.card.components.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.components.R
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.internal.presentation.components.PrimerInput
import io.primer.android.internal.presentation.screens.card.components.input.transformations.ExpiryDateVisualTransformation
import io.primer.android.scope.PrimerCardFormScope
import io.primer.android.ui.core.model.SyncValidationError

private object InputConfigs {

    private const val EXPIRY_MAX_LENGTH = 4

    @Suppress("CyclomaticComplexMethod")
    fun labelResource(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> R.string.primer_components_card_form_name_on_card
        PrimerInputElementType.EXPIRY_DATE -> R.string.primer_components_card_form_expiry_date
        PrimerInputElementType.POSTAL_CODE -> R.string.primer_components_card_form_postal_code
        PrimerInputElementType.COUNTRY_CODE -> R.string.primer_components_card_form_country_code
        PrimerInputElementType.CITY -> R.string.primer_components_card_form_city
        PrimerInputElementType.STATE -> R.string.primer_components_card_form_state
        PrimerInputElementType.ADDRESS_LINE_1 -> R.string.primer_components_card_form_address_line_1
        PrimerInputElementType.ADDRESS_LINE_2 -> R.string.primer_components_card_form_address_line_2
        PrimerInputElementType.PHONE_NUMBER -> R.string.primer_components_card_form_phone_number
        PrimerInputElementType.FIRST_NAME -> R.string.primer_components_card_form_first_name
        PrimerInputElementType.LAST_NAME -> R.string.primer_components_card_form_last_name
        PrimerInputElementType.RETAIL_OUTLET -> R.string.primer_components_card_form_retail_outlet
        PrimerInputElementType.OTP_CODE -> R.string.primer_components_card_form_otp_code
        else -> null
    }

    @Suppress("CyclomaticComplexMethod")
    fun placeholderResource(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> R.string.primer_components_card_form_placeholder_full_name
        PrimerInputElementType.EXPIRY_DATE -> R.string.primer_components_card_form_placeholder_expiry_date
        PrimerInputElementType.POSTAL_CODE -> R.string.primer_components_card_form_placeholder_postal_code
        PrimerInputElementType.COUNTRY_CODE -> R.string.primer_components_card_form_placeholder_country_code
        PrimerInputElementType.CITY -> R.string.primer_components_card_form_placeholder_city
        PrimerInputElementType.STATE -> R.string.primer_components_card_form_placeholder_state
        PrimerInputElementType.ADDRESS_LINE_1 -> R.string.primer_components_card_form_placeholder_address_line_1
        PrimerInputElementType.ADDRESS_LINE_2 -> R.string.primer_components_card_form_placeholder_address_line_2
        PrimerInputElementType.PHONE_NUMBER -> R.string.primer_components_card_form_placeholder_phone_number
        PrimerInputElementType.FIRST_NAME -> R.string.primer_components_card_form_placeholder_first_name
        PrimerInputElementType.LAST_NAME -> R.string.primer_components_card_form_placeholder_last_name
        PrimerInputElementType.RETAIL_OUTLET -> R.string.primer_components_card_form_placeholder_retail_outlet
        PrimerInputElementType.OTP_CODE -> R.string.primer_components_card_form_placeholder_otp_code
        else -> null
    }

    fun keyboardOptions(type: PrimerInputElementType): KeyboardOptions = when (type) {
        PrimerInputElementType.EXPIRY_DATE, PrimerInputElementType.OTP_CODE ->
            KeyboardOptions(keyboardType = KeyboardType.Number)

        PrimerInputElementType.PHONE_NUMBER ->
            KeyboardOptions(keyboardType = KeyboardType.Phone)

        PrimerInputElementType.POSTAL_CODE ->
            KeyboardOptions(keyboardType = KeyboardType.Text)

        else -> KeyboardOptions.Default
    }

    fun visualTransformation(type: PrimerInputElementType): VisualTransformation = when (type) {
        PrimerInputElementType.EXPIRY_DATE -> ExpiryDateVisualTransformation()
        else -> VisualTransformation.None
    }

    fun maxLength(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.EXPIRY_DATE -> EXPIRY_MAX_LENGTH
        else -> null
    }

    fun allowedChars(type: PrimerInputElementType): String? = when (type) {
        PrimerInputElementType.EXPIRY_DATE, PrimerInputElementType.OTP_CODE -> "0123456789"
        else -> null
    }

    fun trailingIcon(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.EXPIRY_DATE -> R.drawable.ic_primer_card_expiry_date
        else -> null
    }
}

@Composable
private fun getInputLabel(type: PrimerInputElementType): String {
    val context = LocalContext.current
    val resourceId = InputConfigs.labelResource(type)
    return resourceId?.let { context.getString(it) } ?: type.field
}

@Composable
private fun getInputPlaceholder(type: PrimerInputElementType): String {
    val context = LocalContext.current
    val resourceId = InputConfigs.placeholderResource(type)
    return resourceId?.let { context.getString(it) } ?: ""
}

@Composable
internal fun resolveErrorMessage(error: SyncValidationError?): String? {
    if (error == null) return null

    val context = LocalContext.current

    return error.errorFormatId?.let { formatId ->
        // Try to get the field name string resource
        val fieldName = try {
            context.getString(error.fieldId)
        } catch (_: Exception) {
            context.getString(R.string.primer_components_card_form_field) // Fallback if fieldId resource doesn't exist
        }
        context.getString(formatId, fieldName)
    } ?: error.errorResId?.let { resId ->
        context.getString(resId)
    }
        // If neither errorFormatId nor errorResId are available, fall back to errorId
        ?: error.errorId
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrimerCardFormScope.CardInput(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
    onValueChange: (String) -> Unit,
) {
    val state by state.collectAsStateWithLifecycle()

    // Check if this field should be shown
    val isFieldRequired = type in state.cardFields || type in state.billingFields
    if (!isFieldRequired) return

    val value = state.data[type] ?: ""
    val error = state.fieldErrors?.find { it.inputElementType == type }

    // Apply essential input filtering while letting validation framework provide feedback
    val processedOnValueChange: (String) -> Unit = { newValue ->
        var processedValue = newValue

        // Apply allowed characters filter for strict input types (like CVV, card numbers)
        InputConfigs.allowedChars(type)?.let { allowedChars ->
            processedValue = newValue.filter { it in allowedChars }
        }

        // Apply max length constraint to prevent excessive input
        InputConfigs.maxLength(type)?.let { maxLength ->
            if (processedValue.length > maxLength) {
                processedValue = processedValue.take(maxLength)
            }
        }

        onValueChange(processedValue)
    }

    PrimerInput(
        value = value,
        onValueChange = processedOnValueChange,
        label = getInputLabel(type),
        placeholder = getInputPlaceholder(type),
        modifier = modifier.fillMaxWidth(),
        error = resolveErrorMessage(error),
        enabled = state.isFormEnabled,
        trailingIcon = {
            InputConfigs.trailingIcon(type)?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                )
            }
        },
        visualTransformation = InputConfigs.visualTransformation(type),
        keyboardOptions = InputConfigs.keyboardOptions(type),
        forceLtrForNumbers = type in listOf(
            PrimerInputElementType.EXPIRY_DATE,
            PrimerInputElementType.OTP_CODE,
            PrimerInputElementType.PHONE_NUMBER,
            PrimerInputElementType.POSTAL_CODE
        ),
        onFocusChange = { hasFocus ->
            onFieldFocusChange(type, hasFocus)
        },
    )
}
