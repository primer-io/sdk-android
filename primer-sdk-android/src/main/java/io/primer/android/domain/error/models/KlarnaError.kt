package io.primer.android.domain.error.models

import io.primer.android.analytics.domain.models.BaseContextParams
import io.primer.android.analytics.domain.models.ErrorContextParams
import io.primer.android.data.configuration.models.PaymentMethodType
import java.util.UUID

internal sealed class KlarnaError : PrimerError() {

    object UserUnapprovedError : KlarnaError()

    class KlarnaSdkError(
        val message: String
    ) : KlarnaError()

    override val errorId: String
        get() = when (this) {
            is UserUnapprovedError -> "klarna-user-not-approved"
            is KlarnaSdkError -> "klarna-sdk-error"
        }

    override val description: String
        get() = when (this) {
            is UserUnapprovedError ->
                "User is not approved to perform Klarna payments (diagnosticsId: $diagnosticsId)"
            is KlarnaSdkError ->
                "Multiple errors occurred: $message (diagnosticsId: $diagnosticsId)"
        }

    override val diagnosticsId: String
        get() = UUID.randomUUID().toString()

    override val errorCode: String? = null

    override val exposedError: PrimerError
        get() = this

    override val recoverySuggestion: String?
        get() = null

    override val context: BaseContextParams get() =
        ErrorContextParams(errorId, PaymentMethodType.KLARNA.name)
}
