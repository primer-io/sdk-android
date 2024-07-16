package io.primer.android.components.presentation.paymentMethods.nativeUi.stripe.ach.delegate

import io.primer.android.domain.action.ActionInteractor
import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.models.ConfigurationParams
import io.primer.android.extensions.flatMap
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first

internal class StripeAchClientSessionPatchDelegate(
    private val configurationInteractor: ConfigurationInteractor,
    private val actionInteractor: ActionInteractor
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        emailAddress: String
    ): Result<Unit> = runSuspendCatching {
        configurationInteractor.invoke(ConfigurationParams(true))
            .first()
    }.flatMap {
        it.clientSession
            .clientSessionDataResponse
            .customer.let { customer ->
                val shouldPatchFirstName = customer?.firstName != firstName
                val shouldPatchLastName = customer?.lastName != lastName
                val shouldPatchEmailAddress = customer?.emailAddress != emailAddress
                if (shouldPatchFirstName || shouldPatchLastName || shouldPatchEmailAddress) {
                    runSuspendCatching {
                        actionInteractor.invoke(
                            ActionUpdateCustomerDetailsParams(
                                firstName = firstName.takeIf { shouldPatchFirstName },
                                lastName = lastName.takeIf { shouldPatchLastName },
                                emailAddress = emailAddress.takeIf { shouldPatchEmailAddress }
                            )
                        ).collect()
                    }
                } else {
                    Result.success(Unit)
                }
            }
    }
}
