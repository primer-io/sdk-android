package io.primer.android.analytics.domain.models

import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.model.dto.PaymentMethodType

internal data class UIAnalyticsParams(
    val action: AnalyticsAction,
    val objectType: ObjectType,
    val place: Place,
    val objectId: ObjectId? = null,
    val context: BaseContextParams? = null
) : BaseAnalyticsParams()

internal abstract class BaseContextParams

internal data class PaymentMethodContextParams(val paymentMethodType: PaymentMethodType) :
    BaseContextParams()

internal data class BankIssuerContextParams(val issuerId: String) : BaseContextParams()

internal data class PaymentInstrumentIdContextParams(val id: String) : BaseContextParams()

internal data class UrlContextParams(val url: String) : BaseContextParams()
