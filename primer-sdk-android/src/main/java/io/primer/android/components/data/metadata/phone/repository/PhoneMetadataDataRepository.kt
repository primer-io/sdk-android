package io.primer.android.components.data.metadata.phone.repository

import io.primer.android.components.data.metadata.phone.datasource.RemotePhoneMetadataDataSource
import io.primer.android.components.domain.payments.metadata.phone.exception.PhoneValidationException
import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadata
import io.primer.android.components.domain.payments.metadata.phone.repository.PhoneMetadataRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.extensions.mapSuspendCatching
import io.primer.android.extensions.runSuspendCatching

internal class PhoneMetadataDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val remoteMetadataDataSource: RemotePhoneMetadataDataSource
) : PhoneMetadataRepository {

    override suspend fun getPhoneMetadata(phoneNumber: String) =
        runSuspendCatching {
            when (phoneNumber.isBlank()) {
                true -> throw PhoneValidationException("Phone number cannot be blank.")
                false ->
                    remoteMetadataDataSource.execute(
                        BaseRemoteRequest(
                            localConfigurationDataSource.getConfiguration(),
                            phoneNumber
                        )
                    )
            }
        }.mapSuspendCatching { metadataResponse ->
            when (metadataResponse.isValid) {
                true -> PhoneMetadata(
                    requireNotNull(metadataResponse.countryCode),
                    requireNotNull(metadataResponse.nationalNumber)
                )

                false -> throw PhoneValidationException("Failed to parse phone number.")
            }
        }
}
