package io.primer.android.data.token.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.token.validation.ValidationTokenDataSource
import io.primer.android.data.token.validation.model.toValidationTokenData
import io.primer.android.domain.token.repository.ValidateTokenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single

internal class ValidateTokenDataRepository(
    private val configurationDataSource: LocalConfigurationDataSource,
    private val validateDataSource: ValidationTokenDataSource,
) : ValidateTokenRepository {

    override fun validate(clientToken: String): Flow<Boolean> {
        return configurationDataSource.get().map { config ->
            validateDataSource.execute(
                BaseRemoteRequest(
                    config,
                    clientToken.toValidationTokenData()
                )
            ).single().success ?: false
        }
    }
}
