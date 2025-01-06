package io.primer.android.paymentmethods.manager.composable

/**
 * An interface representing a headless component that can submit collected data by [PrimerHeadlessDataCollectable].
 */
interface PrimerHeadlessSubmitable {
    fun submit()
}
