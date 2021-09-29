package io.primer.android.di

import io.primer.android.data.session.datasource.LocalClientSessionDataSource
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.events.EventDispatcher
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource
import io.primer.android.data.token.model.ClientToken
import io.primer.android.data.token.repository.ClientTokenDataRepository
import io.primer.android.domain.token.repository.ClientTokenRepository
import io.primer.android.model.dto.PrimerConfig
import io.primer.android.threeds.data.repository.PaymentMethodDataRepository
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import org.koin.dsl.module

internal val CheckoutConfigModule = { config: PrimerConfig, clientToken: ClientToken ->
    module {
        single { config }
        single { config.theme }

        single {
            LocalClientTokenDataSource(clientToken)
        }

        single<ClientTokenRepository> { ClientTokenDataRepository(get()) }

        single {
            LocalClientSessionDataSource()
        }
        single<PaymentMethodRepository> { PaymentMethodDataRepository() }

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
