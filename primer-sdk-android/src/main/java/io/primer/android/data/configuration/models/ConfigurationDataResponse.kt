@file:Suppress("MaxLineLength")

package io.primer.android.data.configuration.models

import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableBoolean
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
import io.primer.android.data.configuration.models.CheckoutModuleDataResponse.Companion.OPTIONS_FIELD
import io.primer.android.data.configuration.models.CheckoutModuleDataResponse.Companion.REQUEST_URL_FIELD
import io.primer.android.data.configuration.models.CheckoutModuleDataResponse.Companion.TYPE_FIELD
import io.primer.android.data.payments.displayMetadata.model.IconDisplayMetadata
import io.primer.android.domain.session.models.PaymentMethodConfig
import org.json.JSONObject

internal data class ConfigurationDataResponse(
    val pciUrl: String,
    val coreUrl: String,
    val paymentMethods: List<PaymentMethodConfigDataResponse>,
    val checkoutModules: List<CheckoutModuleDataResponse>,
    val keys: ConfigurationKeysDataResponse?,
    val clientSession: ClientSessionDataResponse?,
    val environment: Environment,
    val primerAccountId: String?
) : JSONDeserializable {

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

    companion object {
        private const val PCI_URL_FIELD = "pciUrl"
        private const val CORE_URL_FIELD = "coreUrl"
        const val PAYMENT_METHODS_CONFIG_FIELD = "paymentMethods"
        private const val CHECKOUT_MODULES_FIELD = "checkoutModules"
        private const val CONFIGURATION_KEYS_FIELD = "keys"
        private const val CLIENT_SESSION_FIELD = "clientSession"
        const val ENVIRONMENT_FIELD = "env"
        const val PRIMER_ACCOUNT_ID_FIELD = "primerAccountId"

        val provider = object : WhitelistedHttpBodyKeysProvider {
            // ktlint-disable max-line-length
            override val values: List<WhitelistedKey> = whitelistedKeys {
                primitiveKey(CORE_URL_FIELD)
                primitiveKey(PCI_URL_FIELD)
                nonPrimitiveKey(CHECKOUT_MODULES_FIELD) {
                    primitiveKey(TYPE_FIELD)
                    primitiveKey(REQUEST_URL_FIELD)
                    primitiveKey(OPTIONS_FIELD)
                }
                nonPrimitiveKey(CLIENT_SESSION_FIELD) {
                    primitiveKey(ClientSessionDataResponse.CLIENT_SESSION_ID_FIELD)
                    nonPrimitiveKey(ClientSessionDataResponse.ORDER_DATA_FIELD) {
                        primitiveKey(OrderDataResponse.ORDER_ID_FIELD)
                        primitiveKey(OrderDataResponse.CURRENCY_CODE_FIELD)
                        primitiveKey(OrderDataResponse.MERCHANT_AMOUNT_FIELD)
                        primitiveKey(OrderDataResponse.TOTAL_ORDER_AMOUNT_FIELD)
                        primitiveKey(OrderDataResponse.COUNTRY_CODE_FIELD)
                        nonPrimitiveKey(OrderDataResponse.LINE_ITEMS_FIELD) {
                            primitiveKey(OrderDataResponse.LineItemDataResponse.ITEM_ID_FIELD)
                            primitiveKey(OrderDataResponse.LineItemDataResponse.UNIT_AMOUNT_FIELD)
                            primitiveKey(OrderDataResponse.LineItemDataResponse.QUANTITY_FIELD)
                            primitiveKey(
                                OrderDataResponse.LineItemDataResponse.DISCOUNT_AMOUNT_FIELD
                            )
                            primitiveKey(OrderDataResponse.LineItemDataResponse.TAX_AMOUNT_FIELD)
                            primitiveKey(OrderDataResponse.LineItemDataResponse.TAX_CODE_FIELD)
                        }
                        primitiveKey(OrderDataResponse.FEES_FIELD)
                    }
                    nonPrimitiveKey(ClientSessionDataResponse.PAYMENT_METHOD_DATA_FIELD) {
                        primitiveKey(
                            ClientSessionDataResponse.PaymentMethodDataResponse.VAULT_ON_SUCCESS_FIELD
                        )
                        nonPrimitiveKey(
                            ClientSessionDataResponse.PaymentMethodDataResponse.OPTIONS_FIELD
                        ) {
                            primitiveKey(
                                ClientSessionDataResponse.PaymentMethodOptionDataResponse.TYPE_FIELD
                            )
                            primitiveKey(
                                ClientSessionDataResponse.PaymentMethodOptionDataResponse.SURCHARGE_FIELD
                            )
                            nonPrimitiveKey(
                                ClientSessionDataResponse.PaymentMethodOptionDataResponse.NETWORKS_FIELD
                            ) {
                                primitiveKey(
                                    ClientSessionDataResponse.NetworkOptionDataResponse.TYPE_FIELD
                                )
                                primitiveKey(
                                    ClientSessionDataResponse.NetworkOptionDataResponse.SURCHARGE_FIELD
                                )
                            }
                        }
                        primitiveKey("orderedAllowedCardNetworks") // not yet defined as a constant
                    }
                }
                primitiveKey(PRIMER_ACCOUNT_ID_FIELD)
                primitiveKey(ENVIRONMENT_FIELD)
                nonPrimitiveKey(PAYMENT_METHODS_CONFIG_FIELD) {
                    primitiveKey(PaymentMethodConfigDataResponse.ID_FIELD)
                    primitiveKey(PaymentMethodConfigDataResponse.NAME_FIELD)
                    primitiveKey(PaymentMethodConfigDataResponse.IMPLEMENTATION_TYPE_FIELD)
                    primitiveKey(PaymentMethodConfigDataResponse.TYPE_FIELD)
                    nonPrimitiveKey(PaymentMethodConfigDataResponse.DISPLAY_METADATA_FIELD) {
                        nonPrimitiveKey(PaymentMethodDisplayMetadataResponse.BUTTON_DATA_FIELD) {
                            nonPrimitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ICON_URL_DATA_FIELD
                            ) {
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.IconUrlDataResponse.COLORED_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.IconUrlDataResponse.LIGHT_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.IconUrlDataResponse.DARK_FIELD
                                )
                            }
                            nonPrimitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.BACKGROUND_COLOR_DATA_FIELD
                            ) {
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.COLORED_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.LIGHT_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.DARK_FIELD
                                )
                            }
                            nonPrimitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.BORDER_COLOR_DATA_FIELD
                            ) {
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.COLORED_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.LIGHT_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.DARK_FIELD
                                )
                            }
                            nonPrimitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.BORDER_WIDTH_DATA_FIELD
                            ) {
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.BorderWidthDataResponse.COLORED_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.BorderWidthDataResponse.LIGHT_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.BorderWidthDataResponse.DARK_FIELD
                                )
                            }
                            primitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.CORNER_RADIUS_FIELD
                            )
                            primitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.TEXT_FIELD
                            )
                            nonPrimitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.TEXT_COLOR_DATA_FIELD
                            ) {
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.COLORED_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.LIGHT_FIELD
                                )
                                primitiveKey(
                                    PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse.DARK_FIELD
                                )
                            }
                            primitiveKey(
                                PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ICON_POSITION_FIELD
                            )
                        }
                    }
                    primitiveKey("processorConfigId") // not yet defined as a constant
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<ConfigurationDataResponse> {

            override fun deserialize(t: JSONObject): ConfigurationDataResponse {
                return ConfigurationDataResponse(
                    t.getString(PCI_URL_FIELD),
                    t.getString(CORE_URL_FIELD),
                    t.getJSONArray(PAYMENT_METHODS_CONFIG_FIELD).sequence<JSONObject>()
                        .map {
                            JSONSerializationUtils
                                .getDeserializer<PaymentMethodConfigDataResponse>()
                                .deserialize(it)
                        }.toList(),
                    t.getJSONArray(CHECKOUT_MODULES_FIELD).sequence<JSONObject>()
                        .map {
                            JSONSerializationUtils
                                .getDeserializer<CheckoutModuleDataResponse>()
                                .deserialize(it)
                        }.toList(),
                    t.optJSONObject(CONFIGURATION_KEYS_FIELD)?.let {
                        JSONSerializationUtils
                            .getDeserializer<ConfigurationKeysDataResponse>()
                            .deserialize(it)
                    },
                    JSONSerializationUtils
                        .getDeserializer<ClientSessionDataResponse>()
                        .deserialize(t.getJSONObject(CLIENT_SESSION_FIELD)),
                    Environment.valueOf(t.getString(ENVIRONMENT_FIELD)),
                    t.optNullableString(PRIMER_ACCOUNT_ID_FIELD)
                )
            }
        }
    }
}

internal data class PaymentMethodConfigDataResponse(
    val id: String?, // payment card has null only
    val name: String?,
    val implementationType: PaymentMethodImplementationType,
    val type: String,
    val options: PaymentMethodRemoteConfigOptions?,
    val displayMetadata: PaymentMethodDisplayMetadataResponse?
) : JSONDeserializable {
    fun toPaymentMethodConfig() = PaymentMethodConfig(type)

    companion object {
        const val ID_FIELD = "id"
        const val NAME_FIELD = "name"
        const val IMPLEMENTATION_TYPE_FIELD = "implementationType"
        const val TYPE_FIELD = "type"
        private const val OPTIONS_FIELD = "options"
        const val DISPLAY_METADATA_FIELD = "displayMetadata"

        @JvmField
        val deserializer = object : JSONDeserializer<PaymentMethodConfigDataResponse> {

            override fun deserialize(t: JSONObject): PaymentMethodConfigDataResponse {
                return PaymentMethodConfigDataResponse(
                    t.optNullableString(ID_FIELD),
                    t.optNullableString(NAME_FIELD),
                    PaymentMethodImplementationType.safeValueOf(
                        t.optNullableString(
                            IMPLEMENTATION_TYPE_FIELD
                        )
                    ),
                    t.getString(TYPE_FIELD),
                    t.optJSONObject(OPTIONS_FIELD)?.let {
                        JSONSerializationUtils
                            .getDeserializer<PaymentMethodRemoteConfigOptions>()
                            .deserialize(it)
                    },
                    t.optJSONObject(DISPLAY_METADATA_FIELD)?.let {
                        JSONSerializationUtils
                            .getDeserializer<PaymentMethodDisplayMetadataResponse>()
                            .deserialize(it)
                    }
                )
            }
        }
    }
}

internal enum class PaymentMethodImplementationType {
    NATIVE_SDK,
    WEB_REDIRECT,
    IPAY88_SDK,
    UNKNOWN;

    companion object {
        fun safeValueOf(type: String?) = values().find { type == it.name } ?: UNKNOWN
    }
}

internal data class PaymentMethodRemoteConfigOptions(
    val merchantId: String?,
    val merchantAccountId: String?,
    val merchantAppId: String?,
    val threeDSecureEnabled: Boolean?
) : JSONDeserializable {

    companion object {
        private const val MERCHANT_ID_FIELD = "merchantId"
        private const val MERCHANT_ACCOUNT_ID_FIELD = "merchantAccountId"
        private const val THREE_DS_SECURE_ENABLED_FIELD = "threeDSecureEnabled"
        private const val MERCHANT_APP_ID_FIELD = "appId"

        @JvmField
        val deserializer = JSONDeserializer { t ->
            PaymentMethodRemoteConfigOptions(
                t.optNullableString(MERCHANT_ID_FIELD),
                t.optNullableString(MERCHANT_ACCOUNT_ID_FIELD),
                t.optNullableString(MERCHANT_APP_ID_FIELD),
                t.optNullableBoolean(THREE_DS_SECURE_ENABLED_FIELD)
            )
        }
    }
}

internal enum class Environment(val environment: String) {
    LOCAL_DOCKER("local_dev"),
    DEV("dev"),
    SANDBOX("sandbox"),
    STAGING("staging"),
    PRODUCTION("production")
}
