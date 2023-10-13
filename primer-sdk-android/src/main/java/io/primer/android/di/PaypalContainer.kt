package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalConfirmBillingAgreementDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalCreateBillingAgreementDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalCreateOrderDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalCheckoutConfigurationDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalConfirmBillingAgreementDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateBillingAgreementDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateOrderDataRepository
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository.PaypalOrderInfoDataRepository
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
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalCheckoutOrderInfoValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalCheckoutOrderValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalValidBillingAgreementTokenRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalValidOrderAmountRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalValidOrderCurrencyRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalValidOrderTokenRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation.PaypalVaultValidationRulesResolver

internal class PaypalContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerSingleton { RemotePaypalOrderInfoDataSource(sdk.resolve()) }

        registerSingleton<PaypalInfoRepository> {
            PaypalOrderInfoDataRepository(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            PaypalOrderInfoInteractor(
                resolve()
            )
        }

        registerSingleton<PaypalCheckoutConfigurationRepository> {
            PaypalCheckoutConfigurationDataRepository(
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            PaypalCheckoutConfigurationInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<PaypalVaultConfigurationRepository> {
            PaypalVaultConfigurationDataRepository(
                sdk.resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            PaypalVaultConfigurationInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { RemotePaypalCreateOrderDataSource(sdk.resolve()) }

        registerSingleton<PaypalCreateOrderRepository> {
            PaypalCreateOrderDataRepository(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            PaypalCreateOrderInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { RemotePaypalCreateBillingAgreementDataSource(sdk.resolve()) }

        registerSingleton { RemotePaypalConfirmBillingAgreementDataSource(sdk.resolve()) }

        registerSingleton<PaypalCreateBillingAgreementRepository> {
            PaypalCreateBillingAgreementDataRepository(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton<PaypalConfirmBillingAgreementRepository> {
            PaypalConfirmBillingAgreementDataRepository(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            PaypalCreateBillingAgreementInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton {
            PaypalConfirmBillingAgreementInteractor(
                resolve(),
                sdk.resolve()
            )
        }

        registerSingleton { PaypalValidOrderTokenRule() }

        registerSingleton {
            PaypalCheckoutOrderInfoValidationRulesResolver(
                resolve()
            )
        }

        registerSingleton { PaypalValidBillingAgreementTokenRule() }

        registerSingleton {
            PaypalVaultValidationRulesResolver(
                resolve()
            )
        }

        registerSingleton { PaypalValidOrderAmountRule() }

        registerSingleton { PaypalValidOrderCurrencyRule() }

        registerSingleton { PaypalCheckoutOrderValidationRulesResolver(resolve(), resolve()) }
    }
}
