package io.primer.android.errors.di

import io.primer.android.core.di.DependencyContainer
import io.primer.android.core.di.SdkContainer
import io.primer.android.errors.data.mapper.DefaultErrorMapperRegistry
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.errors.domain.DefaultErrorResolver
import io.primer.android.errors.domain.ErrorMapperRegistry

class ErrorResolverContainer(private val sdk: () -> SdkContainer) : DependencyContainer() {
    override fun registerInitialDependencies() {
        registerSingleton<ErrorMapperRegistry> { DefaultErrorMapperRegistry() }

        registerFactory<BaseErrorResolver> {
            DefaultErrorResolver(
                errorMapperRegistry = resolve(),
                logReporter = sdk().resolve(),
            )
        }
    }
}
