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
    private val context: Context,
    private val mapper: KlarnaMapper,
    private val primerConfig: PrimerConfig,
) : KlarnaRepository {

    private lateinit var klarnaComponent: KlarnaComponent

    override suspend fun start(viewModelStoreOwner: ViewModelStoreOwner) {
        klarnaComponent = PrimerHeadlessUniversalCheckoutKlarnaManager(viewModelStoreOwner)
            .provideKlarnaComponent(PrimerSessionIntent.CHECKOUT)
        klarnaComponent.start()
    }

    override suspend fun selectPaymentCategory(category: KlarnaCategory) {
        val klarnaCategory = mapper.mapCategoryToDomain(category)
        val returnIntentUrl = requireNotNull(
            primerConfig.settings.paymentMethodOptions.klarnaOptions.returnIntentUrl,
        ) { "Klarna returnIntentUrl must be configured in PrimerSettings.paymentMethodOptions.klarnaOptions" }

        klarnaComponent.updateCollectedData(
            KlarnaPaymentCollectableData.PaymentOptions(
                context = context,
                returnIntentUrl = returnIntentUrl,
                paymentCategory = klarnaCategory,
            ),
        )
    }

    override suspend fun authorizePayment() = klarnaComponent.submit()

    override suspend fun finalizePayment() = klarnaComponent.updateCollectedData(
        KlarnaPaymentCollectableData.FinalizePayment,
    )

    override val stepFlow
        get() = klarnaComponent.componentStep.map { mapper.mapStep(it) }

    override val errorFlow
        get() = klarnaComponent.componentError.map { it.description }
}
