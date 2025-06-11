package io.primer.composable.internal.presentation.screens.card.components.input.transformations

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import io.primer.cardShared.CardNumberFormatter

/**
 * Visual transformation for card numbers using CardNumberFormatter from payment-card-shared
 * Caches formatter instances for better performance
 */
internal class CardNumberVisualTransformation : VisualTransformation {
    private var cachedInput: String = ""
    private var cachedFormatter: CardNumberFormatter? = null
    private var cachedFormatted: String = ""

    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text

        // Use cached formatter if input hasn't changed
        val formatted = if (input == cachedInput && cachedFormatter != null) {
            cachedFormatted
        } else {
            val formatter = CardNumberFormatter.fromString(input, autoInsert = true)
            val formattedText = formatter.toString()

            // Cache for next use
            cachedInput = input
            cachedFormatter = formatter
            cachedFormatted = formattedText

            formattedText
        }

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = CardNumberOffsetMapping(input, formatted),
        )
    }
}

/**
 * Offset mapping for card number formatting (handles spaces)
 */
private class CardNumberOffsetMapping(
    private val original: String,
    private val formatted: String,
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (offset == 0) return 0

        var transformedOffset = 0
        var originalIndex = 0

        for (char in formatted) {
            if (char == ' ') {
                transformedOffset++
            } else if (originalIndex < offset && originalIndex < original.length) {
                transformedOffset++
                originalIndex++
            } else {
                break
            }
        }

        return transformedOffset
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (offset == 0) return 0

        var originalOffset = 0
        var transformedIndex = 0

        for (char in formatted) {
            if (transformedIndex >= offset) break

            if (char != ' ') {
                originalOffset++
            }
            transformedIndex++
        }

        return originalOffset.coerceAtMost(original.length)
    }
}
