package io.primer.android.analytics.domain.models

import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place
import io.primer.android.payment.dummy.DummyDecisionType

internal data class UIAnalyticsParams(
    val action: AnalyticsAction,
    val objectType: ObjectType,
    val place: Place,
    val objectId: ObjectId? = null,
    val context: BaseContextParams? = null
) : BaseAnalyticsParams()

internal sealed class BaseContextParams

internal data class PaymentMethodContextParams(val paymentMethodType: String) :
    BaseContextParams()

internal data class BankIssuerContextParams(val issuerId: String) : BaseContextParams()

internal data class PaymentInstrumentIdContextParams(val id: String) : BaseContextParams()

internal data class UrlContextParams(val url: String) : BaseContextParams()

internal data class DummyApmDecisionParams(val decision: DummyDecisionType) : BaseContextParams()

internal data class IPay88PaymentMethodContextParams(
    val iPay88PaymentMethodId: String,
    val iPay88ActionType: String,
    val paymentMethodType: String
) : BaseContextParams()

internal open class ThreeDsFailureContextParams(
    open val threeDsSdkVersion: String?,
    open val initProtocolVersion: String?
) : BaseContextParams()

internal open class ThreeDsRuntimeFailureContextParams(
    override val threeDsSdkVersion: String?,
    override val initProtocolVersion: String,
    open val errorCode: String
) : ThreeDsFailureContextParams(threeDsSdkVersion, initProtocolVersion)

internal data class ThreeDsProtocolFailureContextParams(
    val errorDetails: String,
    val description: String,
    val errorCode: String,
    val errorType: String,
    val component: String,
    val transactionId: String,
    val version: String,
    override val threeDsSdkVersion: String?,
    override val initProtocolVersion: String
) : ThreeDsFailureContextParams(
    threeDsSdkVersion,
    initProtocolVersion
)
