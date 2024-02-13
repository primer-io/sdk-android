package io.primer.android.components.domain.payments.metadata.card.repository

import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.components.domain.payments.metadata.card.model.CardBinMetadata

internal fun interface CardBinMetadataRepository {

    suspend fun getBinMetadata(bin: String, source: ValidationSource): Result<List<CardBinMetadata>>
}
