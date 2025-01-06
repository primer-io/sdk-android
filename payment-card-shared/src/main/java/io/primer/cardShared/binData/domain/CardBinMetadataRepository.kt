package io.primer.cardShared.binData.domain

import io.primer.android.components.domain.core.models.card.ValidationSource

fun interface CardBinMetadataRepository {
    suspend fun getBinMetadata(
        bin: String,
        source: ValidationSource,
    ): Result<List<CardBinMetadata>>
}
