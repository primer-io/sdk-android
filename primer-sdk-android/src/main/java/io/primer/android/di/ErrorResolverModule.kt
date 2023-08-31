package io.primer.android.di

import io.primer.android.data.error.DefaultErrorMapperFactory
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperFactory
import org.koin.dsl.module

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
    }
}
