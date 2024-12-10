package io.primer.android.stripe.ach.implementation.session.presentation

import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.models.ActionUpdateCustomerDetailsParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.ConfigurationInteractor
import io.primer.android.configuration.domain.model.ConfigurationParams
import io.primer.android.core.extensions.flatMap

internal class StripeAchClientSessionPatchDelegate(
    private val configurationInteractor: ConfigurationInteractor,
    private val actionInteractor: ActionInteractor
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        emailAddress: String
    ): Result<Unit> = configurationInteractor.invoke(ConfigurationParams(cachePolicy = CachePolicy.ForceCache))
        .flatMap {
            it.clientSession
                .clientSessionDataResponse
                .customer.let { customer ->
                    val shouldPatchFirstName = customer?.firstName != firstName
                    val shouldPatchLastName = customer?.lastName != lastName
                    val shouldPatchEmailAddress = customer?.emailAddress != emailAddress
                    if (shouldPatchFirstName || shouldPatchLastName || shouldPatchEmailAddress) {
                        actionInteractor(
                            MultipleActionUpdateParams(
                                listOf(
                                    ActionUpdateCustomerDetailsParams(
                                        firstName = firstName.takeIf { shouldPatchFirstName },
                                        lastName = lastName.takeIf { shouldPatchLastName },
                                        emailAddress = emailAddress.takeIf { shouldPatchEmailAddress }
                                    )
                                )
                            )
                        ).map { /* no-op */ }
                    } else {
                        Result.success(Unit)
                    }
                }
        }
}
