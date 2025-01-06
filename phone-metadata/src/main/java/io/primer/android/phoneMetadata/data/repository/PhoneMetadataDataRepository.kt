package io.primer.android.phoneMetadata.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.mapSuspendCatching
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.phoneMetadata.data.datasource.RemotePhoneMetadataDataSource
import io.primer.android.phoneMetadata.domain.exception.PhoneValidationException
import io.primer.android.phoneMetadata.domain.model.PhoneMetadata
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository

internal class PhoneMetadataDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val remoteMetadataDataSource: RemotePhoneMetadataDataSource,
) : PhoneMetadataRepository {
    override suspend fun getPhoneMetadata(phoneNumber: String) =
        runSuspendCatching {
            when (phoneNumber.isBlank()) {
                true -> throw PhoneValidationException("Phone number cannot be blank.")
                false ->
                    remoteMetadataDataSource.execute(
                        BaseRemoteHostRequest(
                            configurationDataSource.get().pciUrl,
                            phoneNumber,
                        ),
                    )
            }
        }.mapSuspendCatching { metadataResponse ->
            when (metadataResponse.isValid) {
                true ->
                    PhoneMetadata(
                        requireNotNull(metadataResponse.countryCode),
                        requireNotNull(metadataResponse.nationalNumber),
                    )

                false -> throw PhoneValidationException("Failed to parse phone number.")
            }
        }
}
