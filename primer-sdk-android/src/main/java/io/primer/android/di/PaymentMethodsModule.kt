package io.primer.android.di

import io.primer.android.payment.PaymentMethodDescriptorFactoryRegistry
import io.primer.android.viewmodel.PaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerPaymentMethodCheckerRegistry
import io.primer.android.viewmodel.PrimerViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val PaymentMethodsModule = {
    module {
        single { PrimerPaymentMethodCheckerRegistry }
        single<PaymentMethodCheckerRegistry> { PrimerPaymentMethodCheckerRegistry }
        single { PaymentMethodDescriptorFactoryRegistry(get()) }

        viewModel { PrimerViewModel(get(), get(), get(), get(), get()) }
    }
}
