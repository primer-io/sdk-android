package io.primer.android.internal.data.mappers

import io.primer.android.internal.domain.models.KlarnaCategory
import io.primer.android.internal.domain.models.KlarnaStep
import io.primer.android.internal.domain.models.KlarnaViewData
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory

/**
 * Maps Klarna SDK types to domain types.
 * This isolates the rest of the app from Klarna SDK dependencies.
 */
internal class KlarnaMapper {

    fun mapCategory(category: KlarnaPaymentCategory): KlarnaCategory {
        return KlarnaCategory(
            id = category.identifier,
            displayName = category.name,
            descriptiveAssetUrl = category.descriptiveAssetUrl,
            standardAssetUrl = category.standardAssetUrl,
        )
    }

    fun mapCategories(categories: List<KlarnaPaymentCategory>): List<KlarnaCategory> {
        return categories.map { mapCategory(it) }
    }

    fun mapCategoryToDomain(domainCategory: KlarnaCategory): KlarnaPaymentCategory {
        return KlarnaPaymentCategory(
            identifier = domainCategory.id,
            name = domainCategory.displayName,
            descriptiveAssetUrl = domainCategory.descriptiveAssetUrl,
            standardAssetUrl = domainCategory.standardAssetUrl,
        )
    }

    fun mapStep(step: KlarnaPaymentStep): KlarnaStep = when (step) {
        is KlarnaPaymentStep.PaymentSessionCreated -> {
            KlarnaStep.CategoriesAvailable(
                categories = mapCategories(step.paymentCategories),
            )
        }
        is KlarnaPaymentStep.PaymentViewLoaded -> {
            KlarnaStep.ViewLoaded(
                viewData = KlarnaViewData(nativeView = step.paymentView),
            )
        }
        is KlarnaPaymentStep.PaymentSessionAuthorized -> {
            KlarnaStep.Authorized(needsFinalization = !step.isFinalized)
        }
        is KlarnaPaymentStep.PaymentSessionFinalized -> {
            KlarnaStep.Finalized
        }
    }
}
