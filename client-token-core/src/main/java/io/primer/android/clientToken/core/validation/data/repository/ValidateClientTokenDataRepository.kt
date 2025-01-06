package io.primer.android.clientToken.core.validation.data.repository

import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.datasource.ValidationTokenDataSource
import io.primer.android.clientToken.core.validation.data.model.toValidationTokenData
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching

internal class ValidateClientTokenDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val validateDataSource: ValidationTokenDataSource,
) : ValidateClientTokenRepository {
    override suspend fun validate(clientToken: String) =
        runSuspendCatching {
            configurationDataSource.get().let { config ->
                validateDataSource.execute(
                    BaseRemoteHostRequest(
                        config.pciUrl,
                        clientToken.toValidationTokenData(),
                    ),
                ).success ?: throw InvalidClientTokenException()
            }
        }
}
