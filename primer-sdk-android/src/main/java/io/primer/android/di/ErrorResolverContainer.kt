package io.primer.android.di

import io.primer.android.data.error.DefaultErrorMapperFactory
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperFactory

internal class ErrorResolverContainer(private val sdk: SdkContainer) : DependencyContainer() {

    override fun registerInitialDependencies() {
        registerFactory<ErrorMapperFactory> { DefaultErrorMapperFactory() }

        registerFactory<BaseErrorEventResolver> {
            CheckoutErrorEventResolver(
                sdk.resolve(),
                resolve(),
                sdk.resolve(),
                sdk.resolve(),
                sdk.resolve()
            )
        }
    }
}
