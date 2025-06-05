package io.primer.composable.internal.presentation.screens.card.components.input.transformations

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import io.primer.cardShared.ExpiryDateFormatter

/**
 * Visual transformation for expiry dates using ExpiryDateFormatter from payment-card-shared
 */
internal class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatter = ExpiryDateFormatter.fromString(text.text, autoInsert = true)
        val formatted = formatter.toString()
        
        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = ExpiryDateOffsetMapping(text.text, formatted)
        )
    }
}

/**
 * Offset mapping for expiry date formatting (handles forward slash)
 */
private class ExpiryDateOffsetMapping(
    private val original: String,
    private val formatted: String
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset == 0) return 0
        
        // If we're past 2 digits and there's a slash in formatted, account for it
        val slashIndex = formatted.indexOf('/')
        return if (slashIndex != -1 && offset > slashIndex) {
            offset + 1
        } else {
            offset
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (offset == 0) return 0
        
        val slashIndex = formatted.indexOf('/')
        return if (slashIndex != -1 && offset > slashIndex) {
            (offset - 1).coerceAtLeast(0)
        } else {
            offset
        }.coerceAtMost(original.length)
    }
}
