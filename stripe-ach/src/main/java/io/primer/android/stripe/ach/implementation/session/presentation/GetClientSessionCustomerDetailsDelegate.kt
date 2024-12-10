package io.primer.android.stripe.ach.implementation.session.presentation

import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.core.extensions.runSuspendCatching

internal class GetClientSessionCustomerDetailsDelegate(
    private val configurationInteractor: ConfigurationInteractor
) {

    suspend operator fun invoke(): Result<ClientSessionCustomerDetails> = runSuspendCatching {
        val customer = configurationInteractor.invoke(ConfigurationParams(CachePolicy.ForceCache)).getOrThrow()
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
