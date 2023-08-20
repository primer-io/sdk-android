package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.optNullableBoolean
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.sequence
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
    val primerAccountId: String?,
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
        private const val PAYMENT_METHODS_CONFIG_FIELD = "paymentMethods"
        private const val CHECKOUT_MODULES_FIELD = "checkoutModules"
        private const val CONFIGURATION_KEYS_FIELD = "keys"
        private const val CLIENT_SESSION_FIELD = "clientSession"
        private const val ENVIRONMENT_FIELD = "env"
        private const val PRIMER_ACCOUNT_ID_FIELD = "primerAccountId"

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
        private const val ID_FIELD = "id"
        private const val NAME_FIELD = "name"
        private const val IMPLEMENTATION_TYPE_FIELD = "implementationType"
        private const val TYPE_FIELD = "type"
        private const val OPTIONS_FIELD = "options"
        private const val DISPLAY_METADATA_FIELD = "displayMetadata"

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
    val threeDSecureEnabled: Boolean?,
) : JSONDeserializable {

    companion object {
        private const val MERCHANT_ID_FIELD = "merchantId"
        private const val MERCHANT_ACCOUNT_ID_FIELD = "merchantAccountId"
        private const val THREE_DS_SECURE_ENABLED_FIELD = "threeDSecureEnabled"
        private const val MERCHANT_APP_ID_FIELD = "merchantAppId"

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
    PRODUCTION("production"),
}
