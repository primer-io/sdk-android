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
import io.primer.checkout.orchestrator.domain.SdkContextProvider
import org.json.JSONObject
import java.util.Locale

internal class DefaultSdkContextProvider(
    private val context: Context,
    private val configurationDataSource: CacheConfigurationDataSource,
    private val checkoutSessionIdProvider: BaseDataProvider<String>,
    private val applicationIdProvider: BaseDataProvider<String>,
    private val settings: PrimerSettings,
    private val analyticsDataProvider: BaseDataProvider<AnalyticsData>,
) : SdkContextProvider {

    @SuppressLint("HardwareIds")
    override fun provide(paymentMethodType: String): String {
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

            put(
                PAYMENT_FIELD,
                JSONObject().apply {
                    put(PAYMENT_METHOD_TYPE_FIELD, paymentMethodType)
                },
            )

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

    private companion object {
        const val SDK_FIELD = "sdk"
        const val TYPE_FIELD = "type"
        const val VERSION_FIELD = "version"
        const val INTEGRATION_TYPE_FIELD = "integrationType"
        const val PAYMENT_HANDLING_FIELD = "paymentHandling"

        const val DEVICE_FIELD = "device"
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

        const val MERCHANT_FIELD = "merchant"
        const val PRIMER_ACCOUNT_ID_FIELD = "primerAccountId"

        const val ANALYTICS_FIELD = "analytics"
        const val URL_FIELD = "url"
    }
}
