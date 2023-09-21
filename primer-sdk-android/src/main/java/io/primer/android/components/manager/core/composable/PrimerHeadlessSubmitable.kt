package io.primer.android.components.manager.core.composable

/**
 * An interface representing a headless component that can submit collected data by [PrimerHeadlessDataCollectable].
 */
interface PrimerHeadlessSubmitable {

    fun submit()
}
