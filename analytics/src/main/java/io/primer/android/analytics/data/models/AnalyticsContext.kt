package io.primer.android.analytics.data.models

import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

sealed class AnalyticsContext(
    val contextType: AnalyticsContextType,
) : BaseAnalyticsProperties() {
    enum class AnalyticsContextType {
        PAYMENT_METHOD,
        URL,
        BANK_ISSUER,
        PROCESSOR_TEST_APM,
        PAYMENT_METHOD_ID,
        IPAY88,
        THREE_DS_FAILURE,
        THREE_DS_RUNTIME_FAILURE,
        THREE_DS_PROTOCOL_FAILURE,
        ERROR,
        CACHE_SOURCE,
        DROP_IN_SOURCE,
    }

    companion object {
        internal const val ANALYTICS_CONTEXT_TYPE_FIELD = "contextType"

        @JvmField
        val serializer =
            JSONObjectSerializer<AnalyticsContext> { t ->
                when (t.contextType) {
                    AnalyticsContextType.PAYMENT_METHOD ->
                        PaymentMethodAnalyticsContext.serializer.serialize(
                            t as PaymentMethodAnalyticsContext,
                        )

                    AnalyticsContextType.URL ->
                        UrlAnalyticsContext.serializer.serialize(
                            t as UrlAnalyticsContext,
                        )

                    AnalyticsContextType.BANK_ISSUER ->
                        BankIssuerAnalyticsContext.serializer.serialize(
                            t as BankIssuerAnalyticsContext,
                        )

                    AnalyticsContextType.PROCESSOR_TEST_APM ->
                        ProcessorTestAnalyticsContext.serializer.serialize(
                            t as ProcessorTestAnalyticsContext,
                        )

                    AnalyticsContextType.PAYMENT_METHOD_ID ->
                        PaymentInstrumentIdAnalyticsContext.serializer.serialize(
                            t as PaymentInstrumentIdAnalyticsContext,
                        )

                    AnalyticsContextType.THREE_DS_FAILURE ->
                        ThreeDsFailureAnalyticsContext.serializer.serialize(
                            t as ThreeDsFailureAnalyticsContext,
                        )

                    AnalyticsContextType.THREE_DS_RUNTIME_FAILURE ->
                        ThreeDsRuntimeFailureAnalyticsContext.serializer.serialize(
                            t as ThreeDsRuntimeFailureAnalyticsContext,
                        )

                    AnalyticsContextType.THREE_DS_PROTOCOL_FAILURE ->
                        ThreeDsProtocolFailureAnalyticsContext.serializer.serialize(
                            t as ThreeDsProtocolFailureAnalyticsContext,
                        )

                    AnalyticsContextType.IPAY88 ->
                        IPay88AnalyticsContext.serializer.serialize(
                            t as IPay88AnalyticsContext,
                        )

                    AnalyticsContextType.ERROR ->
                        ErrorAnalyticsContext.serializer.serialize(
                            t as ErrorAnalyticsContext,
                        )

                    AnalyticsContextType.CACHE_SOURCE ->
                        CacheSourceAnalyticsContext.serializer.serialize(
                            t as CacheSourceAnalyticsContext,
                        )

                    AnalyticsContextType.DROP_IN_SOURCE ->
                        DropInSourceAnalyticsContext.serializer.serialize(
                            t as DropInSourceAnalyticsContext,
                        )
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                when (
                    AnalyticsContextType.valueOf(t.getString(ANALYTICS_CONTEXT_TYPE_FIELD))
                ) {
                    AnalyticsContextType.PAYMENT_METHOD ->
                        PaymentMethodAnalyticsContext.deserializer.deserialize(t)

                    AnalyticsContextType.URL ->
                        UrlAnalyticsContext.deserializer.deserialize(t)

                    AnalyticsContextType.BANK_ISSUER ->
                        BankIssuerAnalyticsContext.deserializer.deserialize(t)

                    AnalyticsContextType.PROCESSOR_TEST_APM ->
                        ProcessorTestAnalyticsContext.deserializer.deserialize(t)

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

                    AnalyticsContextType.ERROR ->
                        ErrorAnalyticsContext.deserializer.deserialize(t)

                    AnalyticsContextType.CACHE_SOURCE ->
                        CacheSourceAnalyticsContext.deserializer.deserialize(t)

                    AnalyticsContextType.DROP_IN_SOURCE ->
                        DropInSourceAnalyticsContext.deserializer.deserialize(t)
                }
            }
    }
}

data class UrlAnalyticsContext(val url: String?, val paymentMethodType: String? = null) :
    AnalyticsContext(AnalyticsContextType.URL) {
    companion object {
        private const val URL_FIELD = "url"
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"

        @JvmField
        val serializer =
            JSONObjectSerializer<UrlAnalyticsContext> { t ->
                JSONObject().apply {
                    putOpt(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    putOpt(URL_FIELD, t.url)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<UrlAnalyticsContext> { t ->
                UrlAnalyticsContext(
                    t.optNullableString(URL_FIELD),
                    t.optNullableString(PAYMENT_METHOD_TYPE_FIELD),
                )
            }
    }
}

internal data class PaymentMethodAnalyticsContext(val paymentMethodType: String) :
    AnalyticsContext(AnalyticsContextType.PAYMENT_METHOD) {
    companion object {
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"

        @JvmField
        val serializer =
            JSONObjectSerializer<PaymentMethodAnalyticsContext> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<PaymentMethodAnalyticsContext> { t ->
                PaymentMethodAnalyticsContext(
                    t.optString(PAYMENT_METHOD_TYPE_FIELD),
                )
            }
    }
}

internal data class BankIssuerAnalyticsContext(val issuerId: String) :
    AnalyticsContext(AnalyticsContextType.BANK_ISSUER) {
    companion object {
        private const val ISSUER_ID_FIELD = "issuerId"

        @JvmField
        val serializer =
            JSONObjectSerializer<BankIssuerAnalyticsContext> { t ->
                JSONObject().apply {
                    put(ISSUER_ID_FIELD, t.issuerId)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<BankIssuerAnalyticsContext> { t ->
                BankIssuerAnalyticsContext(
                    t.optString(ISSUER_ID_FIELD),
                )
            }
    }
}

internal data class ProcessorTestAnalyticsContext(val decision: String) :
    AnalyticsContext(AnalyticsContextType.PROCESSOR_TEST_APM) {
    companion object {
        private const val DECISION_FIELD = "decision"

        @JvmField
        val serializer =
            JSONObjectSerializer<ProcessorTestAnalyticsContext> { t ->
                JSONObject().apply {
                    put(DECISION_FIELD, t.decision)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<ProcessorTestAnalyticsContext> { t ->
                ProcessorTestAnalyticsContext(
                    t.optString(DECISION_FIELD),
                )
            }
    }
}

internal data class PaymentInstrumentIdAnalyticsContext(val paymentMethodId: String) :
    AnalyticsContext(AnalyticsContextType.PAYMENT_METHOD_ID) {
    companion object {
        private const val PAYMENT_METHOD_ID_FIELD = "paymentMethodId"

        @JvmField
        val serializer =
            JSONObjectSerializer<PaymentInstrumentIdAnalyticsContext> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_ID_FIELD, t.paymentMethodId)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PaymentInstrumentIdAnalyticsContext(
                    t.optString(PAYMENT_METHOD_ID_FIELD),
                )
            }
    }
}

data class CacheSourceAnalyticsContext(val source: String) :
    AnalyticsContext(AnalyticsContextType.CACHE_SOURCE) {
    companion object {
        private const val CACHE_SOURCE_FIELD = "source"

        @JvmField
        val serializer =
            JSONObjectSerializer<CacheSourceAnalyticsContext> { t ->
                JSONObject().apply {
                    put(CACHE_SOURCE_FIELD, t.source)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                CacheSourceAnalyticsContext(
                    t.getString(CACHE_SOURCE_FIELD),
                )
            }
    }
}

internal data class IPay88AnalyticsContext(
    val iPay88PaymentMethodId: String,
    val iPay88ActionType: String,
    val paymentMethodType: String,
) : AnalyticsContext(AnalyticsContextType.IPAY88) {
    companion object {
        private const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        private const val IPAY88_PAYMENT_METHOD_ID_FIELD = "iPay88PaymentMethodId"
        private const val IPAY88_ACTION_TYPE_FIELD = "iPay88ActionType"

        @JvmField
        val serializer =
            JSONObjectSerializer<IPay88AnalyticsContext> { t ->
                JSONObject().apply {
                    put(PAYMENT_METHOD_TYPE_FIELD, t.paymentMethodType)
                    put(IPAY88_PAYMENT_METHOD_ID_FIELD, t.iPay88PaymentMethodId)
                    put(IPAY88_ACTION_TYPE_FIELD, t.iPay88ActionType)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                IPay88AnalyticsContext(
                    t.getString(PAYMENT_METHOD_TYPE_FIELD),
                    t.getString(IPAY88_PAYMENT_METHOD_ID_FIELD),
                    t.getString(IPAY88_ACTION_TYPE_FIELD),
                )
            }
    }
}

internal data class ThreeDsFailureAnalyticsContext(
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String?,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) :
    AnalyticsContext(AnalyticsContextType.THREE_DS_FAILURE) {
    companion object {
        private const val SDK_VERSION_FIELD = "threeDsSdkVersion"
        private const val INIT_PROTOCOL_VERSION_FIELD = "initProtocolVersion"
        private const val SDK_WRAPPER_VERSION_FIELD = "threeDsWrapperSdkVersion"
        private const val SDK_PROVIDER_FIELD = "threeDsSdkProvider"

        @JvmField
        val serializer =
            JSONObjectSerializer<ThreeDsFailureAnalyticsContext> { t ->
                JSONObject().apply {
                    putOpt(SDK_VERSION_FIELD, t.threeDsSdkVersion)
                    putOpt(INIT_PROTOCOL_VERSION_FIELD, t.initProtocolVersion)
                    put(SDK_WRAPPER_VERSION_FIELD, t.threeDsWrapperSdkVersion)
                    put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                ThreeDsFailureAnalyticsContext(
                    t.optNullableString(SDK_VERSION_FIELD),
                    t.optNullableString(INIT_PROTOCOL_VERSION_FIELD),
                    t.optString(SDK_WRAPPER_VERSION_FIELD),
                    t.optString(SDK_PROVIDER_FIELD),
                )
            }
    }
}

internal data class ThreeDsRuntimeFailureAnalyticsContext(
    val threeDsSdkVersion: String?,
    val initProtocolVersion: String,
    val errorCode: String?,
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
) :
    AnalyticsContext(AnalyticsContextType.THREE_DS_RUNTIME_FAILURE) {
    companion object {
        private const val SDK_VERSION_FIELD = "threeDsSdkVersion"
        private const val INIT_PROTOCOL_VERSION_FIELD = "initProtocolVersion"
        private const val SDK_WRAPPER_VERSION_FIELD = "threeDsWrapperSdkVersion"
        private const val SDK_PROVIDER_FIELD = "threeDsSdkProvider"
        private const val ERROR_CODE_FIELD = "errorCode"

        @JvmField
        val serializer =
            JSONObjectSerializer<ThreeDsRuntimeFailureAnalyticsContext> { t ->
                JSONObject().apply {
                    putOpt(SDK_VERSION_FIELD, t.threeDsSdkVersion)
                    put(INIT_PROTOCOL_VERSION_FIELD, t.initProtocolVersion)
                    putOpt(ERROR_CODE_FIELD, t.errorCode)
                    put(SDK_WRAPPER_VERSION_FIELD, t.threeDsWrapperSdkVersion)
                    put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<ThreeDsRuntimeFailureAnalyticsContext> { t ->
                ThreeDsRuntimeFailureAnalyticsContext(
                    t.optNullableString(SDK_VERSION_FIELD),
                    t.optString(INIT_PROTOCOL_VERSION_FIELD),
                    t.optNullableString(ERROR_CODE_FIELD),
                    t.optString(SDK_WRAPPER_VERSION_FIELD),
                    t.optString(SDK_PROVIDER_FIELD),
                )
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
    val threeDsWrapperSdkVersion: String,
    val threeDsSdkProvider: String,
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
        val serializer =
            object : JSONObjectSerializer<ThreeDsProtocolFailureAnalyticsContext> {
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
                        put(SDK_PROVIDER_FIELD, t.threeDsSdkProvider)
                        put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                    }
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<ThreeDsProtocolFailureAnalyticsContext> { t ->
                ThreeDsProtocolFailureAnalyticsContext(
                    t.optString(ERROR_DETAILS_FIELD),
                    t.optString(DESCRIPTION_FIELD),
                    t.optString(ERROR_CODE_FIELD),
                    t.optString(ERROR_TYPE_FIELD),
                    t.optString(COMPONENT_FIELD),
                    t.optString(TRANSACTION_FIELD),
                    t.optString(VERSION_FIELD),
                    t.optNullableString(SDK_VERSION_FIELD),
                    t.optString(INIT_PROTOCOL_VERSION_FIELD),
                    t.optString(SDK_WRAPPER_VERSION_FIELD),
                    t.optString(SDK_PROVIDER_FIELD),
                )
            }
    }
}

internal data class ErrorAnalyticsContext(
    val errorId: String,
    val paymentMethodType: String? = null,
) : AnalyticsContext(AnalyticsContextType.ERROR) {
    companion object {
        private const val ERROR_ID = "errorId"
        private const val PAYMENT_METHOD_TYPE = "paymentMethodType"

        @JvmField
        val serializer =
            JSONObjectSerializer<ErrorAnalyticsContext> { t ->
                JSONObject().apply {
                    put(ERROR_ID, t.errorId)
                    put(PAYMENT_METHOD_TYPE, t.paymentMethodType)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                ErrorAnalyticsContext(
                    t.optString(ERROR_ID),
                    t.optNullableString(PAYMENT_METHOD_TYPE),
                )
            }
    }
}

data class DropInSourceAnalyticsContext(
    val source: DropInLoadingSource,
) : AnalyticsContext(AnalyticsContextType.DROP_IN_SOURCE) {
    companion object {
        private const val SOURCE = "source"

        @JvmField
        val serializer =
            JSONObjectSerializer<DropInSourceAnalyticsContext> { t ->
                JSONObject().apply {
                    put(SOURCE, t.source.name)
                    put(ANALYTICS_CONTEXT_TYPE_FIELD, t.contextType.name)
                }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                DropInSourceAnalyticsContext(
                    DropInLoadingSource.valueOf(t.getString(SOURCE)),
                )
            }
    }
}
