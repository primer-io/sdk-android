package io.primer.android.data.action.models

import io.primer.android.data.tokenization.models.BinData
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateCustomerDetailsParams
import io.primer.android.domain.action.models.ActionUpdateEmailAddressParams
import io.primer.android.domain.action.models.ActionUpdateMobileNumberParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateShippingAddressParams
import io.primer.android.domain.action.models.ActionUpdateShippingOptionIdParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.action.models.MultipleActionUpdateParams
import io.primer.android.threeds.data.models.auth.Address

internal fun MultipleActionUpdateParams.toActionData(): List<ClientSessionActionsDataRequest.Action> {
    return params.flatMap { it.toActionData() }
}

internal fun BaseActionUpdateParams.toActionData(): List<ClientSessionActionsDataRequest.Action> = when (this) {
    is ActionUpdateCustomerDetailsParams -> buildList {
        firstName?.let {
            add(ClientSessionActionsDataRequest.SetCustomerFirstName(it))
        }
        lastName?.let {
            add(ClientSessionActionsDataRequest.SetCustomerLastName(it))
        }
        emailAddress?.let {
            add(ClientSessionActionsDataRequest.SetEmailAddress(it))
        }
    }
    is ActionUpdateSelectPaymentMethodParams -> listOf(
        ClientSessionActionsDataRequest.SetPaymentMethod(paymentMethodType, cardNetwork?.let { BinData(it) })
    )
    is ActionUpdateUnselectPaymentMethodParams -> listOf(ClientSessionActionsDataRequest.UnsetPaymentMethod)
    is ActionUpdateBillingAddressParams -> listOf(
        ClientSessionActionsDataRequest.SetBillingAddress(
            Address(
                firstName = firstName,
                lastName = lastName,
                addressLine1 = addressLine1.orEmpty(),
                addressLine2 = addressLine2,
                postalCode = postalCode.orEmpty(),
                city = city.orEmpty(),
                countryCode = countryCode.orEmpty()
            )
        )
    )
    is ActionUpdateMobileNumberParams -> listOf(ClientSessionActionsDataRequest.SetMobileNumber(mobileNumber))
    is ActionUpdateShippingOptionIdParams -> listOf(ClientSessionActionsDataRequest.SetShippingMethodId(id))
    is ActionUpdateShippingAddressParams -> listOf(
        ClientSessionActionsDataRequest.SetShippingAddress(
            Address(
                firstName = firstName,
                lastName = lastName,
                addressLine1 = addressLine1.orEmpty(),
                addressLine2 = addressLine2,
                postalCode = postalCode.orEmpty(),
                city = city.orEmpty(),
                countryCode = countryCode.orEmpty()
            )
        )
    )
    is ActionUpdateEmailAddressParams -> listOf(ClientSessionActionsDataRequest.SetEmailAddress(email))
}
