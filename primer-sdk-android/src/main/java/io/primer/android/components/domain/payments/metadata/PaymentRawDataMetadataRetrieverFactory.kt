package io.primer.android.components.domain.payments.metadata

import io.primer.android.components.domain.core.models.PrimerRawData
import io.primer.android.components.domain.core.models.bancontact.PrimerBancontactCardData
import io.primer.android.components.domain.payments.metadata.card.BancontactCardDataMetadataRetriever
import io.primer.android.components.domain.payments.metadata.empty.EmptyMetadataRetriever
import io.primer.android.di.DISdkComponent
import kotlin.reflect.KClass

internal class PaymentRawDataMetadataRetrieverFactory : DISdkComponent {

    private val registry: Map<KClass<out PrimerRawData>,
        PaymentRawDataMetadataRetriever<PrimerRawData>> = mapOf(
        PrimerBancontactCardData::class to BancontactCardDataMetadataRetriever()
    )

    fun getMetadataRetriever(rawData: PrimerRawData):
        PaymentRawDataMetadataRetriever<PrimerRawData> {
        return registry[rawData::class] ?: EmptyMetadataRetriever()
    }
}
