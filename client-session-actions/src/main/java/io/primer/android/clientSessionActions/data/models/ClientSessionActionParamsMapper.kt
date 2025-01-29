package io.primer.android.clientSessionActions.data.models

import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateCustomerDetailsParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateEmailAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateMobileNumberParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateShippingOptionIdParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.configuration.data.model.AddressData
import io.primer.android.configuration.data.model.CountryCode

internal fun MultipleActionUpdateParams.toActionData(): List<ClientSessionActionsDataRequest.Action> {
    return params.flatMap { it.toActionData() }
}

internal fun BaseActionUpdateParams.toActionData(): List<ClientSessionActionsDataRequest.Action> =
    when (this) {
        is ActionUpdateCustomerDetailsParams ->
            buildList {
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

        is ActionUpdateSelectPaymentMethodParams ->
            listOf(
                ClientSessionActionsDataRequest.SetPaymentMethod(paymentMethodType, cardNetwork?.let { BinData(it) }),
            )

        is ActionUpdateUnselectPaymentMethodParams -> listOf(ClientSessionActionsDataRequest.UnsetPaymentMethod)
        is ActionUpdateBillingAddressParams ->
            listOf(
                ClientSessionActionsDataRequest.SetBillingAddress(
                    AddressData(
                        firstName = firstName,
                        lastName = lastName,
                        addressLine1 = addressLine1.orEmpty(),
                        addressLine2 = addressLine2,
                        postalCode = postalCode.orEmpty(),
                        city = city.orEmpty(),
                        state = state,
                        countryCode = CountryCode.safeValueOf(countryCode.orEmpty()),
                    ),
                ),
            )

        is ActionUpdateMobileNumberParams -> listOf(ClientSessionActionsDataRequest.SetMobileNumber(mobileNumber))
        is ActionUpdateShippingOptionIdParams -> listOf(ClientSessionActionsDataRequest.SetShippingMethodId(id))
        is ActionUpdateShippingAddressParams ->
            listOf(
                ClientSessionActionsDataRequest.SetShippingAddress(
                    AddressData(
                        firstName = firstName,
                        lastName = lastName,
                        addressLine1 = addressLine1.orEmpty(),
                        addressLine2 = addressLine2,
                        postalCode = postalCode.orEmpty(),
                        city = city.orEmpty(),
                        countryCode = CountryCode.safeValueOf(countryCode.orEmpty()),
                    ),
                ),
            )

        is ActionUpdateEmailAddressParams -> listOf(ClientSessionActionsDataRequest.SetEmailAddress(email))
    }
