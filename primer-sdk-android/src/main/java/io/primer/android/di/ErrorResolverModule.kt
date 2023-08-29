package io.primer.android.di

import io.primer.android.components.data.payments.paymentMethods.nolpay.error.NolPayErrorFlowResolver
import io.primer.android.data.error.DefaultErrorMapperFactory
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseErrorFlowResolver
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperFactory
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal const val NOL_PAY_ERROR_RESOLVER_NAME = "nolPayErrorResolver"

internal val errorResolverModule = {
    module {
        factory<ErrorMapperFactory> { DefaultErrorMapperFactory() }
        factory<BaseErrorEventResolver> {
            CheckoutErrorEventResolver(
                get(),
                get(),
                get(),
                get()
            )
        }
        factory<BaseErrorFlowResolver>(named(NOL_PAY_ERROR_RESOLVER_NAME)) {
            NolPayErrorFlowResolver(
                get(),
                get()
            )
        }
    }
}
