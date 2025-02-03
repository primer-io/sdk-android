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

data class ThreeDsFailureContextParams(
    override val errorId: String,
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String?,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) : ErrorContextParams(errorId = errorId)

data class ThreeDsRuntimeFailureContextParams(
    override val errorId: String,
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
    val threeDsErrorCode: String,
) : ErrorContextParams(errorId = errorId)

data class ThreeDsProtocolFailureContextParams(
    override val errorId: String,
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
    val errorDetails: String,
    val description: String,
    val errorCode: String,
    val errorType: String,
    val component: String,
    val transactionId: String,
    val version: String,
) : ErrorContextParams(
    errorId = errorId,
)

open class ErrorContextParams(
    open val errorId: String,
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
