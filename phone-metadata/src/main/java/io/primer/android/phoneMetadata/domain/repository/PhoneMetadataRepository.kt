package io.primer.android.phoneMetadata.domain.repository

import io.primer.android.phoneMetadata.domain.model.PhoneMetadata

fun interface PhoneMetadataRepository {
    suspend fun getPhoneMetadata(phoneNumber: String): Result<PhoneMetadata>
}
