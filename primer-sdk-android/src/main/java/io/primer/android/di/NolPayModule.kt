package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPaySecretDataSource
import io.primer.android.components.data.payments.paymentMethods.nolpay.error.NolPayErrorMapper
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayAppSecretDataRepository
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetCardDetailsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkedCardsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayRequestPaymentInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayTransactionNumberInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayLinkDataValidatorRegistry
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayPaymentDataValidatorRegistry
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayUnlinkDataValidatorRegistry
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayGetLinkedCardsDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayStartPaymentDelegate
import io.primer.android.domain.error.ErrorMapper
import io.primer.nolpay.api.PrimerNolPay
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val NOL_PAY_ERROR_RESOLVER_NAME = "nolPayErrorResolver"

internal val nolPayModule = {
    module {
        factory { NolPayConfigurationInteractor(get()) }

        factory<NolPayConfigurationRepository> { NolPayConfigurationDataRepository(get()) }

        factory { PrimerNolPay }

        factory { RemoteNolPaySecretDataSource(get()) }
        factory<NolPayAppSecretRepository> { NolPayAppSecretDataRepository(get(), get()) }
        factory { NolPayAppSecretInteractor(get(), get()) }
        factory { NolPayGetLinkPaymentCardTokenInteractor(get()) }
        factory { NolPayGetLinkPaymentCardOTPInteractor(get()) }
        factory { NolPayLinkPaymentCardInteractor(get()) }
        factory { NolPayGetUnlinkPaymentCardOTPInteractor(get()) }
        factory { NolPayUnlinkPaymentCardInteractor(get()) }
        factory { NolPayGetCardDetailsInteractor(get()) }
        factory { NolPayRequestPaymentInteractor(get()) }
        factory { NolPayGetLinkedCardsInteractor(get()) }
        factory { NolPayTransactionNumberInteractor(get(), get()) }

        factory { NolPayLinkDataValidatorRegistry() }
        factory { NolPayUnlinkDataValidatorRegistry() }
        factory { NolPayPaymentDataValidatorRegistry() }

        factory<ErrorMapper>(named(NOL_PAY_ERROR_RESOLVER_NAME)) {
            NolPayErrorMapper()
        }

        factory {
            NolPayLinkPaymentCardDelegate(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }

        factory {
            NolPayUnlinkPaymentCardDelegate(
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }

        factory {
            NolPayGetLinkedCardsDelegate(
                get(),
                get(),
                get(),
                get()
            )
        }

        factory {
            NolPayStartPaymentDelegate(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
            )
        }
    }
}
