package io.primer.android.components.domain.payments.metadata.phone

import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadata
import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadataParams
import io.primer.android.components.domain.payments.metadata.phone.repository.PhoneMetadataRepository
import io.primer.android.domain.base.BaseSuspendInteractor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class PhoneMetadataInteractor(
    private val phoneMetadataRepository: PhoneMetadataRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<PhoneMetadata, PhoneMetadataParams>() {

    override suspend fun performAction(params: PhoneMetadataParams): Result<PhoneMetadata> {
        return phoneMetadataRepository.getPhoneMetadata(params.phoneNumber)
    }
}
