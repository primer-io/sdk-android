package io.primer.android.internal.presentation.screens.card.components.input

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.components.R
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.internal.presentation.components.PrimerInput
import io.primer.android.internal.presentation.preview.PreviewContainer
import io.primer.android.internal.presentation.screens.card.components.input.transformations.ExpiryDateVisualTransformation
import io.primer.android.internal.presentation.utils.toWesternNumeralsOnly
import io.primer.android.ui.core.model.SyncValidationError

private object InputConfigs {

    private const val EXPIRY_MAX_LENGTH = 4

    @Suppress("CyclomaticComplexMethod")
    fun labelResource(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> R.string.primer_card_form_label_name
        PrimerInputElementType.EXPIRY_DATE -> R.string.primer_card_form_label_expiry
        PrimerInputElementType.POSTAL_CODE -> R.string.primer_card_form_label_postal
        PrimerInputElementType.COUNTRY_CODE -> R.string.primer_card_form_label_country_code
        PrimerInputElementType.CITY -> R.string.primer_card_form_label_city
        PrimerInputElementType.STATE -> R.string.primer_card_form_label_state
        PrimerInputElementType.ADDRESS_LINE_1 -> R.string.primer_card_form_label_address1
        PrimerInputElementType.ADDRESS_LINE_2 -> R.string.primer_card_form_label_address2
        PrimerInputElementType.PHONE_NUMBER -> R.string.primer_card_form_label_phone
        PrimerInputElementType.FIRST_NAME -> R.string.primer_card_form_label_first_name
        PrimerInputElementType.LAST_NAME -> R.string.primer_card_form_label_last_name
        PrimerInputElementType.RETAIL_OUTLET -> R.string.primer_card_form_label_retail
        PrimerInputElementType.OTP_CODE -> R.string.primer_card_form_label_otp
        else -> null
    }

    @Suppress("CyclomaticComplexMethod")
    fun accessibilityLabelResource(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> R.string.accessibility_card_form_cardholder_name_label
        PrimerInputElementType.EXPIRY_DATE -> R.string.accessibility_card_form_expiry_label
        PrimerInputElementType.POSTAL_CODE -> R.string.accessibility_card_form_billing_address_postal_code_label
        PrimerInputElementType.CITY -> R.string.accessibility_card_form_billing_address_city_label
        PrimerInputElementType.STATE -> R.string.accessibility_card_form_billing_address_state_label
        PrimerInputElementType.ADDRESS_LINE_1 -> R.string.accessibility_card_form_billing_address_address_line_1_label
        PrimerInputElementType.ADDRESS_LINE_2 -> R.string.accessibility_card_form_billing_address_address_line_2_label
        PrimerInputElementType.FIRST_NAME -> R.string.accessibility_card_form_billing_address_first_name_label
        PrimerInputElementType.LAST_NAME -> R.string.accessibility_card_form_billing_address_last_name_label
        PrimerInputElementType.COUNTRY_CODE -> R.string.accessibility_card_form_billing_address_country_label
        else -> null
    }

    fun testId(type: PrimerInputElementType): String = "primer_input_${type.field.lowercase()}"

    fun isRequired(type: PrimerInputElementType): Boolean = when (type) {
        PrimerInputElementType.ADDRESS_LINE_2 -> false
        else -> true
    }

    @Suppress("CyclomaticComplexMethod")
    fun placeholderResource(type: PrimerInputElementType): Int? = when (type) {
        PrimerInputElementType.CARDHOLDER_NAME -> R.string.primer_card_form_placeholder_name
        PrimerInputElementType.EXPIRY_DATE -> R.string.primer_card_form_placeholder_expiry
        PrimerInputElementType.POSTAL_CODE -> R.string.primer_card_form_placeholder_postal
        PrimerInputElementType.COUNTRY_CODE -> R.string.primer_card_form_placeholder_country_code
        PrimerInputElementType.CITY -> R.string.primer_card_form_placeholder_city
        PrimerInputElementType.STATE -> R.string.primer_card_form_placeholder_state
        PrimerInputElementType.ADDRESS_LINE_1 -> R.string.primer_card_form_placeholder_address1
        PrimerInputElementType.ADDRESS_LINE_2 -> R.string.primer_card_form_placeholder_address2
        PrimerInputElementType.PHONE_NUMBER -> R.string.primer_card_form_placeholder_phone
        PrimerInputElementType.FIRST_NAME -> R.string.primer_card_form_placeholder_first_name
        PrimerInputElementType.LAST_NAME -> R.string.primer_card_form_placeholder_last_name
        PrimerInputElementType.RETAIL_OUTLET -> R.string.primer_card_form_placeholder_retail
        PrimerInputElementType.OTP_CODE -> R.string.primer_card_form_placeholder_otp
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

    data class TrailingIconConfig(val iconRes: Int, val contentDescriptionRes: Int?)

    fun trailingIconConfig(type: PrimerInputElementType): TrailingIconConfig? = when (type) {
        PrimerInputElementType.EXPIRY_DATE -> TrailingIconConfig(
            iconRes = R.drawable.ic_primer_card_expiry_date,
            contentDescriptionRes = R.string.accessibility_card_form_expiry_icon,
        )
        else -> null
    }
}

@Composable
private fun getInputLabel(type: PrimerInputElementType): String {
    val resourceId = InputConfigs.labelResource(type)
    return resourceId?.let { stringResource(it) } ?: type.field
}

@Composable
private fun getInputPlaceholder(type: PrimerInputElementType): String {
    val resourceId = InputConfigs.placeholderResource(type)
    return resourceId?.let { stringResource(it) } ?: ""
}

@Composable
private fun getAccessibilityLabel(type: PrimerInputElementType): String? {
    val resourceId = InputConfigs.accessibilityLabelResource(type)
    return resourceId?.let { stringResource(it) }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
internal fun resolveErrorMessage(error: SyncValidationError?): String? {
    if (error == null) return null

    val context = LocalContext.current
    return error.errorFormatId?.let { formatId ->
        // Try to get the field name string resource
        val fieldName = try {
            context.getString(error.fieldId)
        } catch (_: Exception) {
            stringResource(R.string.primer_card_form_label_field) // Fallback if fieldId resource doesn't exist
        }
        stringResource(formatId, fieldName)
    } ?: error.errorResId?.let { resId ->
        stringResource(resId)
    }
        // If neither errorFormatId nor errorResId are available, fall back to errorId
        ?: error.errorId
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardInput(
    type: PrimerInputElementType,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: SyncValidationError? = null,
    enabled: Boolean = true,
    onFocusChange: (Boolean) -> Unit = {},
) {
    // Apply essential input filtering while letting validation framework provide feedback
    val processedOnValueChange: (String) -> Unit = { newValue ->
        var processedValue = newValue

        // Apply allowed characters filter for strict input types (like CVV, card numbers)
        InputConfigs.allowedChars(type)?.let { _ ->
            processedValue = newValue.toWesternNumeralsOnly()
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
        modifier = modifier
            .fillMaxWidth()
            .testTag(InputConfigs.testId(type)),
        error = resolveErrorMessage(error),
        enabled = enabled,
        trailingIcon = {
            InputConfigs.trailingIconConfig(type)?.let { config ->
                Icon(
                    painter = painterResource(id = config.iconRes),
                    contentDescription = config.contentDescriptionRes?.let { stringResource(id = it) },
                )
            }
        },
        visualTransformation = InputConfigs.visualTransformation(type),
        keyboardOptions = InputConfigs.keyboardOptions(type),
        forceLtrForNumbers = type in listOf(
            PrimerInputElementType.PHONE_NUMBER,
            PrimerInputElementType.POSTAL_CODE,
        ),
        onFocusChange = onFocusChange,
        accessibilityLabel = getAccessibilityLabel(type),
        isRequired = InputConfigs.isRequired(type),
    )
}

@Preview(name = "Expiry Date Input", showBackground = true)
@Composable
private fun CardInputExpiryPreview() = PreviewContainer {
    CardInput(type = PrimerInputElementType.EXPIRY_DATE, value = "1225", onValueChange = {})
}

@Preview(name = "Cardholder Name Input", showBackground = true)
@Composable
private fun CardInputNamePreview() = PreviewContainer {
    CardInput(type = PrimerInputElementType.CARDHOLDER_NAME, value = "John Doe", onValueChange = {})
}
