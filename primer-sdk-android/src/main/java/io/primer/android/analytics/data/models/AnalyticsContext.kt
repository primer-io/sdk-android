package io.primer.android.analytics.data.models

import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.payment.dummy.DummyDecisionType
import io.primer.android.threeds.BuildConfig
import io.primer.android.threeds.data.models.postAuth.ThreeDsSdkProvider
import org.json.JSONObject

internal sealed class AnalyticsContext(
    val contextType: AnalyticsContextType
) : BaseAnalyticsProperties() {

    enum class AnalyticsContextType {
        PAYMENT_METHOD,
        URL,
        BANK_ISSUER,
        DUMMY_APM,
        PAYMENT_METHOD_ID,
        IPAY88,
        THREE_DS_FAILURE,
        THREE_DS_RUNTIME_FAILURE,
        THREE_DS_PROTOCOL_FAILURE
    }

    companion object {

        internal const val ANALYTICS_CONTEXT_TYPE_FIELD = "contextType"

        @JvmField
        val serializer = object : JSONObjectSerializer<AnalyticsContext> {
            override fun serialize(t: AnalyticsContext): JSONObject {
                return when (t.contextType) {
                    AnalyticsContextType.PAYMENT_METHOD ->
                        PaymentMethodAnalyticsContext.serializer.serialize(
                            t as PaymentMethodAnalyticsContext
                        )
                    AnalyticsContextType.URL ->
                        UrlAnalyticsContext.serializer.serialize(
                            t as UrlAnalyticsContext
                        )
                    AnalyticsContextType.BANK_ISSUER ->
                        BankIssuerAnalyticsContext.serializer.serialize(
                            t as BankIssuerAnalyticsContext
                        )
                    AnalyticsContextType.DUMMY_APM ->
                        DummyApmAnalyticsContext.serializer.serialize(
                            t as DummyApmAnalyticsContext
                        )
                    AnalyticsContextType.PAYMENT_METHOD_ID ->
                        PaymentInstrumentIdAnalyticsContext.serializer.serialize(
                            t as PaymentInstrumentIdAnalyticsContext
                        )
                    AnalyticsContextType.THREE_DS_FAILURE ->
                        ThreeDsFailureAnalyticsContext.serializer.serialize(
                            t as ThreeDsFailureAnalyticsContext
                        )
                    AnalyticsContextType.THREE_DS_RUNTIME_FAILURE ->
                        ThreeDsRuntimeFailureAnalyticsContext.serializer.serialize(
                            t as ThreeDsRuntimeFailureAnalyticsContext
                        )
                    AnalyticsContextType.THREE_DS_PROTOCOL_FAILURE ->
                        ThreeDsProtocolFailureAnalyticsContext.serializer.serialize(
                            t as ThreeDsProtocolFailureAnalyticsContext
                        )
                    AnalyticsContextType.IPAY88 ->
                        IPay88AnalyticsContext.serializer.serialize(
                            t as IPay88AnalyticsContext
                        )
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<AnalyticsContext> {

            override fun deserialize(t: JSONObject): AnalyticsContext {
                return when (
                    AnalyticsContextType.valueOf(t.getString(ANALYTICS_CONTEXT_TYPE_FIELD))
                ) {
                    AnalyticsContextType.PAYMENT_METHOD ->
                        PaymentMethodAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.URL ->
                        UrlAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.BANK_ISSUER ->
                        BankIssuerAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.DUMMY_APM ->
                        DummyApmAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.PAYMENT_METHOD_ID ->
                        PaymentInstrumentIdAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.THREE_DS_FAILURE ->
                        ThreeDsFailureAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.THREE_DS_RUNTIME_FAILURE ->
                        ThreeDsRuntimeFailureAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.THREE_DS_PROTOCOL_FAILURE ->
                        ThreeDsProtocolFailureAnalyticsContext.deserializer.deserialize(t)
                    AnalyticsContextType.IPAY88 ->
                        IPay88AnalyticsContext.deserializer.deserialize(t)
                }
            }
        }
    }
}

internal data class UrlAnalyticsContext(val url: String?, val paymentMethodType: String? = null) :
    AnalyticsContext(AnalyticsContextType.URL) {

    companion object {

        private const val URL_FIELD = "url"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"

        @JvmField
        val serializer = object : JSONObjectSerializer<UrlAnalyticsContext> {
            override fun serialize(t: UrlAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    putOpt(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    putOpt(URL_FIELD, t.url)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<UrlAnalyticsContext> {
            override fun deserialize(t: JSONObject): UrlAnalyticsContext {
                return UrlAnalyticsContext(
                    t.optNullableString(URL_FIELD),
                    t.optNullableString(PAYMENT_METHOD_TYPE_FIELD)
                )
            }
        }
    }
}

internal data class PaymentMethodAnalyticsContext(val paymentMethodType: String) :
    AnalyticsContext(AnalyticsContextType.PAYMENT_METHOD) {

    companion object {

        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"

        @JvmField
        val serializer = object : JSONObjectSerializer<PaymentMethodAnalyticsContext> {
            override fun serialize(t: PaymentMethodAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<PaymentMethodAnalyticsContext> {
            override fun deserialize(t: JSONObject): PaymentMethodAnalyticsContext {
                return PaymentMethodAnalyticsContext(
                    t.optString(PAYMENT_METHOD_TYPE_FIELD)
                )
            }
        }
    }
}

internal data class BankIssuerAnalyticsContext(val issuerId: String) :
    AnalyticsContext(AnalyticsContextType.BANK_ISSUER) {

    companion object {

        private const val ISSUER_ID_FIELD = "issuerId"

        @JvmField
        val serializer = object : JSONObjectSerializer<BankIssuerAnalyticsContext> {
            override fun serialize(t: BankIssuerAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    put(ISSUER_ID_FIELD, t.issuerId)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<BankIssuerAnalyticsContext> {
            override fun deserialize(t: JSONObject): BankIssuerAnalyticsContext {
                return BankIssuerAnalyticsContext(
                    t.optString(ISSUER_ID_FIELD)
                )
            }
        }
    }
}

internal data class DummyApmAnalyticsContext(val decision: DummyDecisionType) :
    AnalyticsContext(AnalyticsContextType.DUMMY_APM) {

    companion object {

        private const val DECISION_FIELD = "decision"

        @JvmField
        val serializer = object : JSONObjectSerializer<DummyApmAnalyticsContext> {
            override fun serialize(t: DummyApmAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    put(DECISION_FIELD, t.decision.name)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<DummyApmAnalyticsContext> {
            override fun deserialize(t: JSONObject): DummyApmAnalyticsContext {
                return DummyApmAnalyticsContext(
                    t.optString(DECISION_FIELD).let { DummyDecisionType.valueOf(it) }
                )
            }
        }
    }
}

internal data class PaymentInstrumentIdAnalyticsContext(val paymentMethodId: String) :
    AnalyticsContext(AnalyticsContextType.PAYMENT_METHOD_ID) {

    companion object {

        private const val PAYMENT_METHOD_ID_FIELD = "paymentMethodId"

        @JvmField
        val serializer = object : JSONObjectSerializer<PaymentInstrumentIdAnalyticsContext> {
            override fun serialize(t: PaymentInstrumentIdAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_ID_FIELD, t.paymentMethodId)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<PaymentInstrumentIdAnalyticsContext> {
            override fun deserialize(t: JSONObject): PaymentInstrumentIdAnalyticsContext {
                return PaymentInstrumentIdAnalyticsContext(
                    t.optString(PAYMENT_METHOD_ID_FIELD)
                )
            }
        }
    }
}

internal data class IPay88AnalyticsContext(
    val iPay88PaymentMethodId: String,
    val iPay88ActionType: String,
    val paymentMethodType: String
) : AnalyticsContext(AnalyticsContextType.IPAY88) {

    companion object {

        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val IPAY88_PAYMENT_METHOD_ID_FIELD = "iPay88PaymentMethodId"
        private const val IPAY88_ACTION_TYPE_FIELD = "iPay88ActionType"

        @JvmField
        val serializer = object : JSONObjectSerializer<IPay88AnalyticsContext> {
            override fun serialize(t: IPay88AnalyticsContext): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    put(IPAY88_PAYMENT_METHOD_ID_FIELD, t.iPay88PaymentMethodId)
                    put(IPAY88_ACTION_TYPE_FIELD, t.iPay88ActionType)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<IPay88AnalyticsContext> {
            override fun deserialize(t: JSONObject): IPay88AnalyticsContext {
                return IPay88AnalyticsContext(
                    t.getString(PAYMENT_METHOD_TYPE_FIELD),
                    t.getString(IPAY88_PAYMENT_METHOD_ID_FIELD),
                    t.getString(IPAY88_ACTION_TYPE_FIELD)
                )
            }
        }
    }
}

internal data class ThreeDsFailureAnalyticsContext(
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String?,
    val threeDsWrapperSdkVersion: String = BuildConfig.SDK_VERSION_STRING,
    val threeDsSdkProvider: ThreeDsSdkProvider = ThreeDsSdkProvider.NETCETERA
) :
    AnalyticsContext(AnalyticsContextType.THREE_DS_FAILURE) {

    companion object {

        private const val SDK_VERSION_FIELD = "threeDsSdkVersion"
        private const val INIT_PROTOCOL_VERSION_FIELD = "initProtocolVersion"
        private const val SDK_WRAPPER_VERSION_FIELD = "threeDsWrapperSdkVersion"
        private const val SDK_PROVIDER_FIELD = "threeDsSdkProvider"

        @JvmField
        val serializer = object : JSONObjectSerializer<ThreeDsFailureAnalyticsContext> {
            override fun serialize(t: ThreeDsFailureAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    putOpt(SDK_VERSION_FIELD, t.threeDsSdkVersion)
                    putOpt(INIT_PROTOCOL_VERSION_FIELD, t.initProtocolVersion)
                    put(SDK_WRAPPER_VERSION_FIELD, t.threeDsWrapperSdkVersion)
                    put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider.name)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<ThreeDsFailureAnalyticsContext> {
            override fun deserialize(t: JSONObject): ThreeDsFailureAnalyticsContext {
                return ThreeDsFailureAnalyticsContext(
                    t.optNullableString(SDK_VERSION_FIELD),
                    t.optNullableString(INIT_PROTOCOL_VERSION_FIELD),
                    t.optString(SDK_WRAPPER_VERSION_FIELD)
                )
            }
        }
    }
}

internal data class ThreeDsRuntimeFailureAnalyticsContext(
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val errorCode: String?,
    val threeDsWrapperSdkVersion: String = BuildConfig.SDK_VERSION_STRING,
    val threeDsSdkProvider: ThreeDsSdkProvider = ThreeDsSdkProvider.NETCETERA
) :
    AnalyticsContext(AnalyticsContextType.THREE_DS_RUNTIME_FAILURE) {

    companion object {

        private const val SDK_VERSION_FIELD = "threeDsSdkVersion"
        private const val INIT_PROTOCOL_VERSION_FIELD = "initProtocolVersion"
        private const val SDK_WRAPPER_VERSION_FIELD = "threeDsWrapperSdkVersion"
        private const val SDK_PROVIDER_FIELD = "threeDsSdkProvider"
        private const val ERROR_CODE_FIELD = "errorCode"

        @JvmField
        val serializer = object : JSONObjectSerializer<ThreeDsRuntimeFailureAnalyticsContext> {
            override fun serialize(t: ThreeDsRuntimeFailureAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    putOpt(SDK_VERSION_FIELD, t.threeDsSdkVersion)
                    put(INIT_PROTOCOL_VERSION_FIELD, t.initProtocolVersion)
                    putOpt(ERROR_CODE_FIELD, t.errorCode)
                    put(SDK_WRAPPER_VERSION_FIELD, t.threeDsWrapperSdkVersion)
                    put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider.name)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<ThreeDsRuntimeFailureAnalyticsContext> {
            override fun deserialize(t: JSONObject): ThreeDsRuntimeFailureAnalyticsContext {
                return ThreeDsRuntimeFailureAnalyticsContext(
                    t.optNullableString(SDK_VERSION_FIELD),
                    t.optString(INIT_PROTOCOL_VERSION_FIELD),
                    t.optNullableString(ERROR_CODE_FIELD),
                    t.optString(SDK_WRAPPER_VERSION_FIELD)
                )
            }
        }
    }
}

internal data class ThreeDsProtocolFailureAnalyticsContext(
    val errorDetails: String,
    val description: String,
    val errorCode: String,
    val errorType: String,
    val component: String,
    val transactionId: String,
    val version: String,
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val threeDsWrapperSdkVersion: String = BuildConfig.SDK_VERSION_STRING,
    val threeDsSdkProvider: ThreeDsSdkProvider = ThreeDsSdkProvider.NETCETERA
) :
    AnalyticsContext(AnalyticsContextType.THREE_DS_PROTOCOL_FAILURE) {

    companion object {

        private const val ERROR_DETAILS_FIELD = "errorDetails"
        private const val DESCRIPTION_FIELD = "description"
        private const val ERROR_CODE_FIELD = "errorCode"
        private const val ERROR_TYPE_FIELD = "errorType"
        private const val COMPONENT_FIELD = "component"
        private const val TRANSACTION_FIELD = "transactionId"
        private const val VERSION_FIELD = "version"
        private const val SDK_VERSION_FIELD = "threeDsSdkVersion"
        private const val INIT_PROTOCOL_VERSION_FIELD = "initProtocolVersion"
        private const val SDK_WRAPPER_VERSION_FIELD = "threeDsWrapperSdkVersion"
        private const val SDK_PROVIDER_FIELD = "threeDsSdkProvider"

        @JvmField
        val serializer = object : JSONObjectSerializer<ThreeDsProtocolFailureAnalyticsContext> {
            override fun serialize(t: ThreeDsProtocolFailureAnalyticsContext): JSONObject {
                return JSONObject().apply {
                    put(ERROR_DETAILS_FIELD, t.errorDetails)
                    put(DESCRIPTION_FIELD, t.description)
                    put(ERROR_CODE_FIELD, t.errorCode)
                    put(ERROR_TYPE_FIELD, t.errorType)
                    put(COMPONENT_FIELD, t.component)
                    put(TRANSACTION_FIELD, t.transactionId)
                    put(VERSION_FIELD, t.version)
                    putOpt(SDK_VERSION_FIELD, t.threeDsSdkVersion)
                    put(INIT_PROTOCOL_VERSION_FIELD, t.initProtocolVersion)
                    put(SDK_WRAPPER_VERSION_FIELD, t.threeDsWrapperSdkVersion)
                    put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider.name)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<ThreeDsProtocolFailureAnalyticsContext> {
            override fun deserialize(t: JSONObject): ThreeDsProtocolFailureAnalyticsContext {
                return ThreeDsProtocolFailureAnalyticsContext(
                    t.optString(ERROR_DETAILS_FIELD),
                    t.optString(DESCRIPTION_FIELD),
                    t.optString(ERROR_CODE_FIELD),
                    t.optString(ERROR_TYPE_FIELD),
                    t.optString(COMPONENT_FIELD),
                    t.optString(TRANSACTION_FIELD),
                    t.optString(VERSION_FIELD),
                    t.optNullableString(SDK_VERSION_FIELD),
                    t.optString(INIT_PROTOCOL_VERSION_FIELD),
                    t.optString(SDK_WRAPPER_VERSION_FIELD)
                )
            }
        }
    }
}
