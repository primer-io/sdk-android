package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nolpay.datasource.RemoteNolPaySecretDataSource
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayAppSecretDataRepository
import io.primer.android.components.data.payments.paymentMethods.nolpay.repository.NolPayConfigurationDataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayAppSecretInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayConfigurationInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardOTPInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayGetLinkPaymentCardTokenInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.NolPayLinkPaymentCardInteractor
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayAppSecretRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.NolPayDataValidatorRegistry
import org.koin.dsl.module

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

        factory { NolPayDataValidatorRegistry() }
    }
}
