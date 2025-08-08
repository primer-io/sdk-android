package io.primer.sample.demos

import androidx.compose.runtime.remember

// Level 5 - Advanced Custom Implementations
object MixedScopesDemo : CheckoutDemo(
    title = "Mixed Scopes Demo",
    description = "Instead of rendering payment method selection, a merchant can jump directly to card form",
    customizationLevel = 3,
    render = {
        val checkout = remember { this }
        components.paymentMethodSelection.screen = {
            checkout.components.cardForm.Screen()
        }
    }
)
