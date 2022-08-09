package io.primer.android.data.configuration.models

import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata
import io.primer.android.domain.ClientSessionData
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.session.models.CheckoutModule
import io.primer.android.domain.session.models.ClientSession
import io.primer.android.domain.session.models.PaymentMethodConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ConfigurationDataResponse(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodConfigDataResponse>,
    val checkoutModules: List<CheckoutModuleDataResponse> = listOf(),
    val keys: ConfigurationKeys? = null,
    val clientSession: ClientSessionResponse? = null,
    @SerialName("env") val environment: Environment,
    val primerAccountId: String? = null,
) {
    fun toConfigurationData(iconDisplayMetaData: List<Map<String, List<IconDisplayMetadata>>>) =
        ConfigurationData(
            pciUrl,
            coreUrl,
            paymentMethods,
            checkoutModules,
            keys,
            clientSession,
            environment,
            primerAccountId,
            iconDisplayMetaData
        )
}

@Serializable
internal data class PaymentMethodConfigDataResponse(
    val id: String? = null, // payment card has null only
    val name: String? = null,
    val implementationType: PaymentMethodImplementationType =
        PaymentMethodImplementationType.UNKNOWN,
    val type: String,
    val options: PaymentMethodRemoteConfigOptions? = null,
    val displayMetadata: PaymentMethodDisplayMetadataResponse? = null
) {
    fun toPaymentMethodConfig() = PaymentMethodConfig(type)
}

internal enum class PaymentMethodImplementationType {
    NATIVE_SDK,
    WEB_REDIRECT,
    UNKNOWN;
}

@Serializable
internal data class PaymentMethodRemoteConfigOptions(
    val merchantId: String? = null,
    val merchantAccountId: String? = null,
    val threeDSecureEnabled: Boolean? = null,
)

@Serializable
internal data class ClientSessionResponse(
    val clientSessionId: String? = null,
    val customerId: String? = null,
    val orderId: String? = null,
    val amount: Int? = null,
    val currencyCode: String? = null,
    val customer: CustomerDataResponse? = null,
    val order: OrderDataResponse? = null,
    val paymentMethod: PaymentMethod? = null,
) {

    @Serializable
    data class PaymentMethod(
        val vaultOnSuccess: Boolean? = null,
        val options: List<PaymentMethodOption> = listOf(),
    ) {

        val surcharges: Map<String, Int>
            get() {
                val map = mutableMapOf<String, Int>()
                options.forEach { option ->
                    if (option.type == "PAYMENT_CARD") {
                        option.networks?.forEach { network ->
                            map[network.type] = network.surcharge
                        }
                    } else {
                        map[option.type] = option.surcharge ?: 0
                    }
                }
                return map
            }
    }

    // todo: may be better to use sealed class/polymorphism
    @Serializable
    data class PaymentMethodOption(
        val type: String,
        val surcharge: Int? = null,
        val networks: List<NetworkOption>? = null,
    )

    @Serializable
    data class NetworkOption(
        val type: String,
        val surcharge: Int,
    )

    fun toClientSessionData() = ClientSessionData(
        PrimerClientSession(
            customer?.customerId ?: customerId,
            order?.id ?: orderId,
            order?.currency ?: currencyCode,
            order?.totalOrderAmount ?: amount,
            order?.lineItems?.map { it.toLineItem() },
            order?.toOrder(),
            customer?.toCustomer(),
        )
    )

    fun toClientSession() = ClientSession(paymentMethod)
}

internal enum class Environment(val environment: String) {
    LOCAL_DOCKER("local_dev"),
    DEV("dev"),
    SANDBOX("sandbox"),
    STAGING("staging"),
    PRODUCTION("production"),
}

@Serializable
internal data class CheckoutModuleDataResponse(
    val type: CheckoutModuleType = CheckoutModuleType.UNKNOWN,
    val requestUrl: String? = null,
    val options: Map<String, Boolean>? = null,
) {
    fun toCheckoutModule() = CheckoutModule(type, options)
}

@Serializable
internal enum class CheckoutModuleType {

    BILLING_ADDRESS,
    CARD_INFORMATION,
    UNKNOWN
}

@Serializable
internal data class PaymentMethodDisplayMetadataResponse(
    @SerialName("button") internal val buttonData: ButtonDataResponse
) {

    @Serializable
    internal data class ButtonDataResponse(
        val iconUrl: IconUrlDataResponse? = null,
        @SerialName("backgroundColor") val backgroundColorData: ColorDataResponse? = null,
        @SerialName("borderColor") val borderColorData: ColorDataResponse? = null,
        @SerialName("borderWidth") val borderWidthData: BorderWidthData? = null,
        val cornerRadius: Float? = null,
        val text: String? = null,
        @SerialName("textColor") val textColorData: ColorDataResponse? = null,
        val iconPositionRelativeToText: IconPosition = IconPosition.START
    ) {
        @Serializable
        internal data class IconUrlDataResponse(
            val colored: String? = null,
            val light: String? = null,
            val dark: String? = null
        )

        @Serializable
        internal data class ColorDataResponse(
            val colored: String? = null,
            val light: String? = null,
            val dark: String? = null
        )

        @Serializable
        internal data class BorderWidthData(
            val colored: Float? = null,
            val light: Float? = null,
            val dark: Float? = null
        )
    }
}

internal enum class IconPosition {
    START,
    END,
    ABOVE,
    BELOW
}
