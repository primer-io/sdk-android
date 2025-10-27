package io.primer.android.internal.domain.models

import android.view.View

/**
 * Domain representation of a Klarna payment category.
 * Isolates the ViewModel from Klarna SDK types.
 */
internal data class KlarnaCategory(
    val id: String,
    val displayName: String,
    val descriptiveAssetUrl: String,
    val standardAssetUrl: String,
)

/**
 * Domain representation of Klarna payment view data.
 * Wraps the native Android view.
 */
internal data class KlarnaViewData(
    val nativeView: View,
)

/**
 * Domain representation of Klarna payment flow steps.
 * Completely decoupled from Klarna SDK.
 */
internal sealed interface KlarnaStep {
    data class CategoriesAvailable(val categories: List<KlarnaCategory>) : KlarnaStep
    data class ViewLoaded(val viewData: KlarnaViewData) : KlarnaStep
    data class Authorized(val needsFinalization: Boolean) : KlarnaStep
    data object Finalized : KlarnaStep
}
