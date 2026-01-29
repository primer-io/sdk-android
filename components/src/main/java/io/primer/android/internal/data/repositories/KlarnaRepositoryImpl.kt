package io.primer.android.internal.data.repositories

import android.content.Context
import androidx.lifecycle.ViewModelStoreOwner
import io.primer.android.PrimerSessionIntent
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.internal.data.mappers.KlarnaMapper
import io.primer.android.internal.domain.models.KlarnaCategory
import io.primer.android.internal.domain.repositories.KlarnaRepository
import io.primer.android.klarna.PrimerHeadlessUniversalCheckoutKlarnaManager
import io.primer.android.klarna.api.component.KlarnaComponent
import io.primer.android.klarna.api.composable.KlarnaPaymentCollectableData
import kotlinx.coroutines.flow.map

internal class KlarnaRepositoryImpl(
    private val mapper: KlarnaMapper,
    private val primerConfig: PrimerConfig,
) : KlarnaRepository {

    private var klarnaComponent: KlarnaComponent? = null

    private fun requireKlarnaComponent() = requireNotNull(klarnaComponent) {
        "KlarnaComponent not initialized. Call start() first."
    }

    override suspend fun start(viewModelStoreOwner: ViewModelStoreOwner) {
        cleanup()
        klarnaComponent = PrimerHeadlessUniversalCheckoutKlarnaManager(viewModelStoreOwner)
            .provideKlarnaComponent(PrimerSessionIntent.CHECKOUT)
        requireKlarnaComponent().start()
    }

    override suspend fun selectPaymentCategory(context: Context, category: KlarnaCategory) {
        val klarnaCategory = mapper.mapCategoryToDomain(category)
        val returnIntentUrl = requireNotNull(
            primerConfig.settings.paymentMethodOptions.klarnaOptions.returnIntentUrl,
        ) { "Klarna returnIntentUrl must be configured in PrimerSettings.paymentMethodOptions.klarnaOptions" }

        requireKlarnaComponent().updateCollectedData(
            KlarnaPaymentCollectableData.PaymentOptions(
                context = context,
                returnIntentUrl = returnIntentUrl,
                paymentCategory = klarnaCategory,
            ),
        )
    }

    override suspend fun authorizePayment() = requireKlarnaComponent().submit()

    override suspend fun finalizePayment() = requireKlarnaComponent().updateCollectedData(
        KlarnaPaymentCollectableData.FinalizePayment,
    )

    override val stepFlow
        get() = requireKlarnaComponent().componentStep.map { mapper.mapStep(it) }

    override val errorFlow
        get() = requireKlarnaComponent().componentError.map { it.description }

    override fun cleanup() {
        klarnaComponent = null
    }
}
