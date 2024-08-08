package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.primer.android.domain.session.CachePolicy
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.first

internal class GetClientSessionCustomerDetailsDelegate(
    private val configurationInteractor: ConfigurationInteractor
) {
    suspend operator fun invoke(): Result<ClientSessionCustomerDetails> = runSuspendCatching {
        val customer = configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache)).first()
            .clientSession.clientSessionDataResponse.customer
        ClientSessionCustomerDetails(
            firstName = customer?.firstName.orEmpty(),
            lastName = customer?.lastName.orEmpty(),
            emailAddress = customer?.emailAddress.orEmpty()
        )
    }

    data class ClientSessionCustomerDetails(
        val firstName: String,
        val lastName: String,
        val emailAddress: String
    )
}
