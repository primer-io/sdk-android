package io.primer.android.analytics.domain.models

import io.primer.android.analytics.data.models.AnalyticsAction
import io.primer.android.analytics.data.models.ObjectId
import io.primer.android.analytics.data.models.ObjectType
import io.primer.android.analytics.data.models.Place

data class UIAnalyticsParams(
    val action: AnalyticsAction,
    val objectType: ObjectType,
    val place: Place,
    val objectId: ObjectId? = null,
    val context: BaseContextParams? = null,
) : BaseAnalyticsParams()

sealed class BaseContextParams

data class PaymentMethodContextParams(val paymentMethodType: String) :
    BaseContextParams()

data class BankIssuerContextParams(val issuerId: String) : BaseContextParams()

data class PaymentInstrumentIdContextParams(val id: String) : BaseContextParams()

data class UrlContextParams(val url: String) : BaseContextParams()

data class ProcessorTestDecisionParams(val decision: String) : BaseContextParams()

data class IPay88PaymentMethodContextParams(
    val iPay88PaymentMethodId: String,
    val iPay88ActionType: String,
    val paymentMethodType: String,
) : BaseContextParams()

open class ThreeDsFailureContextParams(
    open val threeDsSdkVersion: String?,
    open val initProtocolVersion: String?,
    open val threeDsWrapperSdkVersion: String,
    open val threeDsSdkProvider: String,
) : BaseContextParams()

open class ThreeDsRuntimeFailureContextParams(
    override val threeDsSdkVersion: String?,
    override val initProtocolVersion: String,
    override val threeDsWrapperSdkVersion: String,
    override val threeDsSdkProvider: String,
    open val errorCode: String,
) : ThreeDsFailureContextParams(
        threeDsSdkVersion,
        initProtocolVersion,
        threeDsWrapperSdkVersion,
        threeDsSdkProvider,
    )

data class ThreeDsProtocolFailureContextParams(
    val errorDetails: String,
    val description: String,
    val errorCode: String,
    val errorType: String,
    val component: String,
    val transactionId: String,
    val version: String,
    override val threeDsSdkVersion: String?,
    override val initProtocolVersion: String,
    override val threeDsWrapperSdkVersion: String,
    override val threeDsSdkProvider: String,
) : ThreeDsFailureContextParams(
        threeDsSdkVersion,
        initProtocolVersion,
        threeDsWrapperSdkVersion,
        threeDsSdkProvider,
    )

open class ErrorContextParams(
    val errorId: String,
    val paymentMethodType: String? = null,
) : BaseContextParams() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ErrorContextParams

        if (errorId != other.errorId) return false
        return paymentMethodType == other.paymentMethodType
    }

    override fun hashCode(): Int {
        var result = errorId.hashCode()
        result = 31 * result + paymentMethodType.hashCode()
        return result
    }
}
