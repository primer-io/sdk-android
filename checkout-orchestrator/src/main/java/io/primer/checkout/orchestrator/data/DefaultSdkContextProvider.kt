package io.primer.checkout.orchestrator.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import io.primer.android.analytics.data.helper.SdkTypeResolver
import io.primer.android.analytics.data.models.AnalyticsData
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.data.settings.PrimerSettings
import io.primer.checkout.orchestrator.BuildConfig
import io.primer.checkout.orchestrator.domain.ReturnUriProvider
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import org.json.JSONObject
import java.util.Locale

@Suppress("LongParameterList")
internal class DefaultSdkContextProvider(
    private val context: Context,
    private val configurationDataSource: CacheConfigurationDataSource,
    private val checkoutSessionIdProvider: BaseDataProvider<String>,
    private val applicationIdProvider: BaseDataProvider<String>,
    private val settings: PrimerSettings,
    private val analyticsDataProvider: BaseDataProvider<AnalyticsData>,
    private val returnUriProvider: ReturnUriProvider,
) : SdkContextProvider {

    @SuppressLint("HardwareIds")
    override fun provide(paymentMethodType: String?, paymentId: String?): String {
        val configurationData = configurationDataSource.get()
        val clientSession = configurationData.clientSession
        val analyticsData = analyticsDataProvider.provide()

        return JSONObject().apply {
            put(
                SDK_FIELD,
                JSONObject().apply {
                    put(TYPE_FIELD, SdkTypeResolver().resolve().name)
                    put(VERSION_FIELD, BuildConfig.SDK_VERSION_STRING)
                    put(INTEGRATION_TYPE_FIELD, settings.sdkIntegrationType.name)
                    put(PAYMENT_HANDLING_FIELD, settings.paymentHandling.name)
                },
            )

            put(
                DEVICE_FIELD,
                JSONObject().apply {
                    put(PLATFORM_FIELD, ANDROID_PLATFORM)
                    put(MAKE_FIELD, Build.MANUFACTURER)
                    put(MODEL_FIELD, Build.MODEL)
                    put(MODEL_IDENTIFIER_FIELD, Build.DEVICE)
                    put(PLATFORM_VERSION_FIELD, Build.VERSION.RELEASE)
                    put(
                        UNIQUE_DEVICE_IDENTIFIER_FIELD,
                        Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID,
                        ),
                    )
                    put(LOCALE_FIELD, Locale.getDefault().toLanguageTag())
                },
            )

            put(
                APP_FIELD,
                JSONObject().apply {
                    put(IDENTIFIER_FIELD, applicationIdProvider.provide())
                },
            )

            put(
                SESSION_FIELD,
                JSONObject().apply {
                    put(CHECKOUT_SESSION_ID_FIELD, checkoutSessionIdProvider.provide())
                    put(CLIENT_SESSION_ID_FIELD, clientSession.clientSessionId)
                    putOpt(CUSTOMER_ID_FIELD, clientSession.customerId)
                },
            )

            paymentMethodType?.let { type -> put(PAYMENT_FIELD, payment(type, paymentId)) }

            // Where a redirect brings the buyer back to.
            put(REDIRECT_FIELD, JSONObject().put(RETURN_URL_FIELD, returnUriProvider.provide()))

            put(
                MERCHANT_FIELD,
                JSONObject().apply {
                    putOpt(PRIMER_ACCOUNT_ID_FIELD, configurationData.primerAccountId)
                },
            )

            analyticsData.analyticsUrl?.let { url ->
                put(
                    ANALYTICS_FIELD,
                    JSONObject().apply {
                        put(URL_FIELD, url)
                    },
                )
            }
        }.toString()
    }

    private fun payment(paymentMethodType: String, paymentId: String?) =
        JSONObject().apply {
            put(PAYMENT_METHOD_TYPE_FIELD, paymentMethodType)
            putOpt(PAYMENT_ID_FIELD, paymentId)
        }

    private companion object {
        const val SDK_FIELD = "sdk"
        const val TYPE_FIELD = "type"
        const val VERSION_FIELD = "version"
        const val INTEGRATION_TYPE_FIELD = "integrationType"
        const val PAYMENT_HANDLING_FIELD = "paymentHandling"

        const val DEVICE_FIELD = "device"
        const val PLATFORM_FIELD = "platform"
        const val ANDROID_PLATFORM = "ANDROID"
        const val MAKE_FIELD = "make"
        const val MODEL_FIELD = "model"
        const val MODEL_IDENTIFIER_FIELD = "modelIdentifier"
        const val PLATFORM_VERSION_FIELD = "platformVersion"
        const val UNIQUE_DEVICE_IDENTIFIER_FIELD = "uniqueDeviceIdentifier"
        const val LOCALE_FIELD = "locale"

        const val APP_FIELD = "app"
        const val IDENTIFIER_FIELD = "identifier"

        const val SESSION_FIELD = "session"
        const val CHECKOUT_SESSION_ID_FIELD = "checkoutSessionId"
        const val CLIENT_SESSION_ID_FIELD = "clientSessionId"
        const val CUSTOMER_ID_FIELD = "customerId"

        const val PAYMENT_FIELD = "payment"
        const val PAYMENT_METHOD_TYPE_FIELD = "paymentMethodType"
        const val PAYMENT_ID_FIELD = "paymentId"

        const val REDIRECT_FIELD = "redirect"
        const val RETURN_URL_FIELD = "returnUrl"

        const val MERCHANT_FIELD = "merchant"
        const val PRIMER_ACCOUNT_ID_FIELD = "primerAccountId"

        const val ANALYTICS_FIELD = "analytics"
        const val URL_FIELD = "url"
    }
}
