package io.primer.android.di

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationDataSource
import io.primer.android.data.configuration.repository.ConfigurationDataRepository
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.events.EventDispatcher
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource
import io.primer.android.data.token.model.ClientToken
import io.primer.android.data.token.repository.ClientTokenDataRepository
import io.primer.android.data.token.validation.ValidationTokenDataSource
import io.primer.android.domain.session.ConfigurationInteractor
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.domain.token.ValidateTokenRepository
import io.primer.android.domain.token.ValidateTokenDataRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.logging.DefaultLogger
import io.primer.android.logging.Logger
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.threeds.data.repository.PaymentMethodDataRepository
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val CHECKOUT_SESSION_HANDLER_LOGGER_NAME = "CHECKOUT_SESSION"

internal val CheckoutConfigModule = { config: PrimerConfig, clientToken: ClientToken ->
    module {

        factory<Logger>(named(CHECKOUT_SESSION_HANDLER_LOGGER_NAME)) {
            DefaultLogger(
                CHECKOUT_SESSION_HANDLER_LOGGER_NAME
            )
        }

        single { config }
        single { config.settings }
        single { config.theme }

        single {
            LocalClientTokenDataSource(clientToken)
        }

        single { ValidationTokenDataSource(get()) }

        single<ClientTokenRepository> { ClientTokenDataRepository(get()) }

        single {
            LocalConfigurationDataSource(get())
        }

        single {
            RemoteConfigurationDataSource(get())
        }

        single<ConfigurationRepository> { ConfigurationDataRepository(get(), get(), get()) }

        single<PaymentMethodRepository> { PaymentMethodDataRepository() }

        single<ValidateTokenRepository> { ValidateTokenDataRepository(get(), get(), get()) }

        single {
            ConfigurationInteractor(
                get(),
                get(),
                get(named(CHECKOUT_SESSION_HANDLER_LOGGER_NAME))
            )
        }

        single {
            EventDispatcher()
        }

        single {
            MetaDataSource(
                get(),
            )
        }
    }
}
