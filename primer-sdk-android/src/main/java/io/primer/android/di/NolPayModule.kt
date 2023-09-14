package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPaySecretDataSource
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayLinkPaymentCardDelegate
import io.primer.android.components.presentation.paymentMethods.nolpay.delegate.NolPayUnlinkPaymentCardDelegate
import io.primer.android.components.data.payments.paymentMethods.nolpay.error.NolPayErrorFlowResolver
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayAppSecretDataRepository
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetCardDetailsInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetUnlinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayUnlinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import io.primer.android.domain.base.BaseErrorFlowResolver
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val NOL_PAY_ERROR_RESOLVER_NAME = "nolPayErrorResolver"

internal val nolPayModule = {
    module {
        factory {
            NolPayConfigurationInteractor(get())
        }

        factory<NolPayConfigurationRepository> { NolPayConfigurationDataRepository(get()) }

        factory { RemoteNolPaySecretDataSource(get()) }
        factory<NolPayAppSecretRepository> { NolPayAppSecretDataRepository(get()) }
        factory { NolPayAppSecretInteractor(get()) }
        factory { NolPayGetLinkPaymentCardTokenInteractor() }
        factory { NolPayGetLinkPaymentCardOTPInteractor() }
        factory { NolPayLinkPaymentCardInteractor() }
        factory { NolPayGetUnlinkPaymentCardOTPInteractor() }
        factory { NolPayUnlinkPaymentCardInteractor() }
        factory { NolPayGetCardDetailsInteractor() }

        factory { NolPayDataValidatorRegistry() }

        factory<BaseErrorFlowResolver>(named(NOL_PAY_ERROR_RESOLVER_NAME)) {
            NolPayErrorFlowResolver(
                get(),
                get()
            )
        }

        factory {
            NolPayLinkPaymentCardDelegate(
                get(),
                get(),
                get(),
            )
        }

        factory {
            NolPayUnlinkPaymentCardDelegate(
                get(),
                get(),
            )
        }
    }
}
