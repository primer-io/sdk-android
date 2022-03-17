package io.primer.android.domain.token

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.token.validation.ValidationTokenDataSource
import io.primer.android.data.token.validation.model.toValidationTokenData
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.toCheckoutErrorEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single

internal class ValidateTokenDataRepository(
    private val configurationDataSource: LocalConfigurationDataSource,
    private val validateDataSource: ValidationTokenDataSource,
    private val eventDispatcher: EventDispatcher
) : ValidateTokenRepository {

    override fun validate(clientToken: String): Flow<Boolean> {
        return configurationDataSource.get().map { config ->
            validateDataSource.execute(
                BaseRemoteRequest(
                    config,
                    clientToken.toValidationTokenData()
                )
            ).catch { e ->
                eventDispatcher.dispatchEvent(e.toCheckoutErrorEvent())
            }.single().success ?: false
        }
    }
}
