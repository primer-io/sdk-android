package io.primer.android.internal.presentation.screens.card.components.input.transformations

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private const val MONTH_LENGTH = 2

/**
 * Simple visual transformation for expiry dates
 * Adds "/" after 2 characters: MM/YY
 */
internal class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text

        // Simple formatting: add "/" after 2 characters
        val formatted = when {
            input.length <= MONTH_LENGTH -> input
            else -> "${input.take(MONTH_LENGTH)}/${input.drop(MONTH_LENGTH)}"
        }

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = ExpiryDateOffsetMapping(input.length, formatted.length),
        )
    }
}

/**
 * Simple offset mapping for expiry date formatting
 */
private class ExpiryDateOffsetMapping(
    private val originalLength: Int,
    private val formattedLength: Int,
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        return when {
            offset <= MONTH_LENGTH -> offset
            else -> offset + 1 // Account for the "/" character
        }.coerceIn(0, formattedLength)
    }

    override fun transformedToOriginal(offset: Int): Int {
        return when {
            offset <= MONTH_LENGTH -> offset
            offset == MONTH_LENGTH + 1 -> MONTH_LENGTH // The "/" position maps to end of month
            else -> offset - 1 // Account for the "/" character
        }.coerceIn(0, originalLength)
    }
}
