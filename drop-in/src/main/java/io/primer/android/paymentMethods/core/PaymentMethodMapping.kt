package io.primer.android.paymentMethods.core

import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.assets.ui.registry.BrandRegistry
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.paymentMethods.bancontact.AdyenBancontactDropInDescriptor
import io.primer.android.paymentMethods.banks.descriptor.BankIssuerDropInDescriptor
import io.primer.android.paymentMethods.card.descriptors.CardDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.paymentMethods.core.ui.descriptors.UiOptions
import io.primer.android.paymentMethods.klarna.TestKlarnaDropInPaymentMethodDescriptor
import io.primer.android.paymentMethods.klarna.descriptors.KlarnaDropInDescriptor
import io.primer.android.paymentMethods.multibanco.AdyenMultibancoDropInDescriptor
import io.primer.android.paymentMethods.nativeUi.descriptors.NativeUiDropInDescriptor
import io.primer.android.paymentMethods.otp.OtpDropInDescriptor
import io.primer.android.paymentMethods.paypal.TestPayPalDropInPaymentMethodDescriptor
import io.primer.android.paymentMethods.phoneNumber.descriptor.PhoneNumberDropInDescriptor
import io.primer.android.paymentMethods.sofort.TestSofortDropInPaymentMethodDescriptor
import io.primer.android.paymentMethods.stripe.ach.descriptors.StripeAchDropInDescriptor
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal interface PaymentMethodMapping {

    fun getPaymentMethodDescriptorFor(
        paymentMethodType: String,
        paymentMethodName: String?,
        paymentMethodManagerCategory: PrimerPaymentMethodManagerCategory
    ): PaymentMethodDropInDescriptor
}

internal class DefaultPaymentMethodMapping(
    private val config: PrimerConfig,
    private val brandRegistry: BrandRegistry
) : PaymentMethodMapping {

    override fun getPaymentMethodDescriptorFor(
        paymentMethodType: String,
        paymentMethodName: String?,
        paymentMethodManagerCategory: PrimerPaymentMethodManagerCategory
    ): PaymentMethodDropInDescriptor = when (paymentMethodManagerCategory) {
        PrimerPaymentMethodManagerCategory.NATIVE_UI ->
            when (paymentMethodType) {
                PaymentMethodType.ADYEN_MULTIBANCO.name -> AdyenMultibancoDropInDescriptor(
                    uiOptions = createUiOptions(),
                    brandRegistry = brandRegistry,
                    sessionIntent = config.paymentMethodIntent
                )

                PaymentMethodType.PRIMER_TEST_SOFORT.name -> TestSofortDropInPaymentMethodDescriptor(
                    paymentMethodType = paymentMethodType,
                    uiOptions = createUiOptions(),
                    brand = brandRegistry.getBrand(paymentMethodType)
                )

                PaymentMethodType.PRIMER_TEST_PAYPAL.name -> TestPayPalDropInPaymentMethodDescriptor(
                    paymentMethodType = paymentMethodType,
                    uiOptions = createUiOptions(),
                    brand = brandRegistry.getBrand(paymentMethodType)
                )

                else -> NativeUiDropInDescriptor(
                    paymentMethodType = paymentMethodType,
                    uiOptions = createUiOptions(),
                    primerSessionIntent = config.paymentMethodIntent,
                    brandRegistry = brandRegistry
                )
            }

        PrimerPaymentMethodManagerCategory.RAW_DATA ->
            when (paymentMethodType) {
                PaymentMethodType.ADYEN_MBWAY.name -> PhoneNumberDropInDescriptor(
                    paymentMethodType = paymentMethodType,
                    uiOptions = createUiOptions(),
                    paymentMethodName = paymentMethodName,
                    brandRegistry = brandRegistry
                )

                PaymentMethodType.ADYEN_BANCONTACT_CARD.name -> AdyenBancontactDropInDescriptor(
                    uiOptions = createUiOptions(),
                    brandRegistry = brandRegistry
                )

                PaymentMethodType.ADYEN_BLIK.name -> OtpDropInDescriptor(
                    uiOptions = createUiOptions(),
                    paymentMethodType = paymentMethodType
                )

                else -> CardDropInDescriptor(
                    paymentMethodType = paymentMethodType,
                    uiOptions = createUiOptions()
                )
            }

        PrimerPaymentMethodManagerCategory.STRIPE_ACH -> StripeAchDropInDescriptor(
            paymentMethodType = paymentMethodType,
            uiOptions = createUiOptions()
        )

        PrimerPaymentMethodManagerCategory.COMPONENT_WITH_REDIRECT -> BankIssuerDropInDescriptor(
            paymentMethodType = paymentMethodType,
            uiOptions = createUiOptions()
        )

        PrimerPaymentMethodManagerCategory.KLARNA -> {
            when (paymentMethodType) {
                PaymentMethodType.KLARNA.name -> KlarnaDropInDescriptor(
                    paymentMethodType = paymentMethodType,
                    uiOptions = createUiOptions()
                )

                PaymentMethodType.PRIMER_TEST_KLARNA.name -> {
                    TestKlarnaDropInPaymentMethodDescriptor(
                        paymentMethodType = paymentMethodType,
                        uiOptions = createUiOptions(),
                        brand = brandRegistry.getBrand(paymentMethodType)
                    )
                }

                else -> error("Unsupported payment method type '$paymentMethodType'")
            }
        }

        else -> error("Unsupported payment method category '$paymentMethodManagerCategory'")
    }

    private fun createUiOptions() = UiOptions(
        isStandalonePaymentMethod = config.isStandalonePaymentMethod,
        isInitScreenEnabled = config.settings.uiOptions.isInitScreenEnabled,
        isDarkMode = config.settings.uiOptions.theme.isDarkMode
    )
}
