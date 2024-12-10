package io.primer.android.phoneMetadata.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.phoneMetadata.domain.model.PhoneMetadata
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class PhoneMetadataInteractor(
    private val phoneMetadataRepository: PhoneMetadataRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PhoneMetadata, PhoneMetadataParams>() {

    override suspend fun performAction(params: PhoneMetadataParams): Result<PhoneMetadata> {
        return phoneMetadataRepository.getPhoneMetadata(params.phoneNumber)
    }
}
