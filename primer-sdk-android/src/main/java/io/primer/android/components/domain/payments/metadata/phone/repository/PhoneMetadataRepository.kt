package io.primer.android.components.domain.payments.metadata.phone.repository

import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadata

internal fun interface PhoneMetadataRepository {

    suspend fun getPhoneMetadata(phoneNumber: String): Result<PhoneMetadata>
}
