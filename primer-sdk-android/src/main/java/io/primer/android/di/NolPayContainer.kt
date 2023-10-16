package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPayCompletePaymentDataSource
import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPaySecretDataSource
import io.primer.android.components.data.payments.paymentMethods.nolpay.error.NolPayErrorMapper
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayAppSecretDataRepository
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayCompletePaymentDataRepository
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayCompletePaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetCardDetailsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkedCardsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequestPaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPaySdkInitInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequiredActionInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayCompletePaymentRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayUnlinkDataValidatorRegistry
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayGetLinkedCardsDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate
import io.primer.android.domain.error.ErrorMapper
import io.primer.nolpay.api.PrimerNolPay

internal class NolPayContainer(private val sdk: SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerFactory<NolPayConfigurationRepository> {
            NolPayConfigurationDataRepository(sdk.resolve())
        }

        registerFactory { PrimerNolPay }

        registerFactory { RemoteNolPaySecretDataSource(sdk.resolve()) }
        registerFactory { RemoteNolPayCompletePaymentDataSource(sdk.resolve()) }

        registerFactory<NolPayAppSecretRepository> {
            NolPayAppSecretDataRepository(
                sdk.resolve(),
                resolve()
            )
        }

        registerFactory<NolPayCompletePaymentRepository> {
            NolPayCompletePaymentDataRepository(
                resolve()
            )
        }

        registerSingleton { NolPaySdkInitInteractor(resolve(), resolve(), resolve()) }

        registerFactory { NolPayGetLinkPaymentCardTokenInteractor(resolve()) }
        registerFactory { NolPayGetLinkPaymentCardOTPInteractor(resolve()) }
        registerFactory { NolPayLinkPaymentCardInteractor(resolve()) }
        registerFactory { NolPayGetUnlinkPaymentCardOTPInteractor(resolve()) }
        registerFactory { NolPayUnlinkPaymentCardInteractor(resolve()) }
        registerFactory { NolPayGetCardDetailsInteractor(resolve()) }
        registerFactory { NolPayRequestPaymentInteractor(resolve()) }
        registerFactory { NolPayGetLinkedCardsInteractor(resolve()) }
        registerFactory { NolPayRequiredActionInteractor(sdk.resolve(), sdk.resolve()) }
        registerFactory { NolPayCompletePaymentInteractor(resolve()) }

        registerFactory { NolPayLinkDataValidatorRegistry() }
        registerFactory { NolPayUnlinkDataValidatorRegistry() }
        registerFactory { NolPayPaymentDataValidatorRegistry() }

        registerFactory<ErrorMapper>(NOL_PAY_ERROR_RESOLVER_NAME) {
            NolPayErrorMapper()
        }

        registerFactory {
            NolPayLinkPaymentCardDelegate(
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve(),
                resolve()
            )
        }

        registerFactory {
            NolPayUnlinkPaymentCardDelegate(
                resolve(),
                resolve(),
                sdk.resolve(),
                resolve()
            )
        }

        registerFactory {
            NolPayGetLinkedCardsDelegate(
                resolve(),
                sdk.resolve(),
                resolve()
            )
        }

        registerFactory {
            NolPayStartPaymentDelegate(
                sdk.resolve(),
                sdk.resolve(),
                resolve(),
                resolve(),
                resolve(),
                resolve(),
                sdk.resolve()
            )
        }
    }

    internal companion object {

        const val NOL_PAY_ERROR_RESOLVER_NAME = "nolPayErrorResolver"
    }
}
