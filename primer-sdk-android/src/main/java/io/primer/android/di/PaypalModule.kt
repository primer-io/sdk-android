package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalConfirmBillingAgreementDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalCreateBillingAgreementDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalCreateOrderDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalCheckoutConfigurationDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalConfirmBillingAgreementDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateBillingAgreementDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateOrderDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalVaultConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalCheckoutConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalConfirmBillingAgreementInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalCreateBillingAgreementInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalCreateOrderInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalOrderInfoInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.PaypalVaultConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCheckoutConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalConfirmBillingAgreementRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateBillingAgreementRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateOrderRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalInfoRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalVaultConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalCheckoutValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalValidBillingAgreementTokenRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalValidOrderTokenRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalVaultValidationRulesResolver
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalOrderInfoDataRepository
import org.koin.dsl.module

internal val paypalModule = {
    module {
        single { RemotePaypalOrderInfoDataSource(get()) }
        single<PaypalInfoRepository> { PaypalOrderInfoDataRepository(get(), get()) }
        single {
            PaypalOrderInfoInteractor(
                get()
            )
        }

        single<PaypalCheckoutConfigurationRepository> {
            PaypalCheckoutConfigurationDataRepository(
                get(),
                get(),
                get()
            )
        }
        single {
            PaypalCheckoutConfigurationInteractor(
                get(),
                get()
            )
        }

        single<PaypalVaultConfigurationRepository> {
            PaypalVaultConfigurationDataRepository(
                get(),
                get()
            )
        }
        single {
            PaypalVaultConfigurationInteractor(
                get(),
                get()
            )
        }

        single { RemotePaypalCreateOrderDataSource(get()) }
        single<PaypalCreateOrderRepository> { PaypalCreateOrderDataRepository(get(), get()) }
        single {
            PaypalCreateOrderInteractor(
                get(),
                get()
            )
        }

        single { RemotePaypalCreateBillingAgreementDataSource(get()) }
        single { RemotePaypalConfirmBillingAgreementDataSource(get()) }

        single<PaypalCreateBillingAgreementRepository> {
            PaypalCreateBillingAgreementDataRepository(
                get(),
                get()
            )
        }
        single<PaypalConfirmBillingAgreementRepository> {
            PaypalConfirmBillingAgreementDataRepository(
                get(),
                get()
            )
        }
        single {
            PaypalCreateBillingAgreementInteractor(
                get(),
                get()
            )
        }
        single {
            PaypalConfirmBillingAgreementInteractor(
                get(),
                get()
            )
        }

        single { PaypalValidOrderTokenRule() }
        single {
            PaypalCheckoutValidationRulesResolver(
                get()
            )
        }

        single { PaypalValidBillingAgreementTokenRule() }
        single {
            PaypalVaultValidationRulesResolver(
                get()
            )
        }
    }
}
