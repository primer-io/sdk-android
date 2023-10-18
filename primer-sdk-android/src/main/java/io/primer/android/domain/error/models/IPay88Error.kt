package io.primer.android.domain.error.models

import java.util.UUID

internal sealed class IPay88Error : PrimerError() {

    class IPaySdkPaymentFailedError(
        val transactionId: String?,
        val refNo: String?,
        val errorDescription: String?
    ) : IPay88Error()

    object IPaySdkConnectionError : IPay88Error()

    override val errorId: String
        get() = when (this) {
            is IPaySdkPaymentFailedError -> "payment-failed"
            is IPaySdkConnectionError -> "ipay-sdk-connection-error"
        }

    override val description: String
        get() = when (this) {
            is IPaySdkPaymentFailedError ->
                """
                     iPay88 payment (transId: $transactionId, refNo: $refNo
                     failed with error: $errorDescription 
                     diagnosticsId: $diagnosticsId)  
                """.trimIndent()
            is IPaySdkConnectionError ->
                "IPay SDK connection error occurred: (diagnosticsId: $diagnosticsId)"
        }

    override val errorCode: String? = null

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = null
}
