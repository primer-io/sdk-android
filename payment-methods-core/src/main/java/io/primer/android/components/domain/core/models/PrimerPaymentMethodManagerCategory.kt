// package structure is kept in order to maintain backward compatibility
package io.primer.android.components.domain.core.models

enum class PrimerPaymentMethodManagerCategory {
    NATIVE_UI,
    RAW_DATA,
    NOL_PAY,
    KLARNA,
    STRIPE_ACH,
    COMPONENT_WITH_REDIRECT,
}
