package io.primer.android.data.action.models

import io.primer.android.data.tokenization.models.BinData
import io.primer.android.domain.action.models.ActionUpdateBillingAddressParams
import io.primer.android.domain.action.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.domain.action.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.threeds.data.models.Address
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

internal val actionSerializationModule: SerializersModule = SerializersModule {
    polymorphic(ClientSessionActionsDataRequest.Action::class) {
        subclass(
            ClientSessionActionsDataRequest.SetPaymentMethod::class,
            ClientSessionActionsDataRequest.SetPaymentMethod.serializer()
        )
        subclass(
            ClientSessionActionsDataRequest.UnsetPaymentMethod::class,
            ClientSessionActionsDataRequest.UnsetPaymentMethod.serializer()
        )
        subclass(
            ClientSessionActionsDataRequest.SetBillingAddress::class,
            ClientSessionActionsDataRequest.SetBillingAddress.serializer()
        )
    }
}

@Serializable
internal data class ClientSessionActionsDataRequest(
    val actions: List<Action>
) {
    @Serializable
    sealed class Action

    @Serializable
    @SerialName("SELECT_PAYMENT_METHOD")
    data class SetPaymentMethod(val params: SetPaymentMethodRequestDataParams) : Action()

    @Serializable
    data class SetPaymentMethodRequestDataParams(
        val paymentMethodType: String,
        val binData: BinData? = null
    )

    @Serializable
    @SerialName("UNSELECT_PAYMENT_METHOD")
    data class UnsetPaymentMethod(val params: Unit = Unit) : Action()

    @Serializable
    @SerialName("SET_BILLING_ADDRESS")
    class SetBillingAddress(
        val params: SetBillingAddressRequestDataParams
    ) : Action()

    @Serializable
    data class SetBillingAddressRequestDataParams(
        val billingAddress: Address
    )
}

internal fun BaseActionUpdateParams.toActionData() = when (this) {
    is ActionUpdateSelectPaymentMethodParams -> ClientSessionActionsDataRequest.SetPaymentMethod(
        ClientSessionActionsDataRequest.SetPaymentMethodRequestDataParams(
            paymentMethodType,
            cardNetwork?.let { BinData(it) }
        )
    )
    is ActionUpdateUnselectPaymentMethodParams ->
        ClientSessionActionsDataRequest.UnsetPaymentMethod()
    is ActionUpdateBillingAddressParams -> ClientSessionActionsDataRequest.SetBillingAddress(
        ClientSessionActionsDataRequest.SetBillingAddressRequestDataParams(
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
    else -> throw IllegalStateException("Unsupported action mapping for $this.")
}
