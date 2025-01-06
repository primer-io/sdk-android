package io.primer.cardShared.binData.data.repository

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.primer.android.components.domain.core.models.card.ValidationSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.cardShared.binData.data.datasource.InMemoryCardBinMetadataDataSource
import io.primer.cardShared.binData.data.datasource.RemoteCardBinMetadataDataSource
import io.primer.cardShared.binData.data.model.CardNetworkDataResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CardBinMetadataDataRepositoryTest {
    private lateinit var localConfigurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var remoteCardBinMetadataDataSource: RemoteCardBinMetadataDataSource
    private lateinit var inMemoryCardBinMetadataDataSource: InMemoryCardBinMetadataDataSource
    private lateinit var repository: CardBinMetadataDataRepository

    @BeforeEach
    fun setUp() {
        localConfigurationDataSource = mockk()
        remoteCardBinMetadataDataSource = mockk()
        inMemoryCardBinMetadataDataSource = mockk()
        repository =
            CardBinMetadataDataRepository(
                localConfigurationDataSource,
                remoteCardBinMetadataDataSource,
                inMemoryCardBinMetadataDataSource,
            )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getBinMetadata fetches from remote source when ValidationSource is REMOTE`() =
        runTest {
            val bin = "123456"
            val url = "https://example.com/binData"
            val remoteResponse = listOf(CardNetworkDataResponse("Visa", null))

            coEvery { localConfigurationDataSource.get() } returns
                mockk<ConfigurationData> {
                    every { binDataUrl } returns url
                }
            coEvery { inMemoryCardBinMetadataDataSource.get() } returns emptyMap()
            coEvery { remoteCardBinMetadataDataSource.execute(any()) } returns remoteResponse
            coEvery { inMemoryCardBinMetadataDataSource.update(any()) } just Runs

            val result = repository.getBinMetadata(bin, ValidationSource.REMOTE).getOrThrow()

            coVerify { remoteCardBinMetadataDataSource.execute(any()) }
            coVerify { inMemoryCardBinMetadataDataSource.update(any()) }

            assertEquals(1, result.size)
            assertEquals(CardNetwork.Type.VISA.name.titlecase(), result[0].displayName)
        }

    @Test
    fun `getBinMetadata fetches from local source when ValidationSource is LOCAL`() =
        runTest {
            mockkObject(CardNetwork.Companion)
            val bin = "123456"
            val cardDescriptors =
                listOf(
                    mockk<CardNetwork.Descriptor> {
                        every { type } returns CardNetwork.Type.VISA
                    },
                    mockk<CardNetwork.Descriptor> {
                        every { type } returns CardNetwork.Type.MASTERCARD
                    },
                )

            every { CardNetwork.lookupAll(bin) } returns cardDescriptors
            val result = repository.getBinMetadata(bin, ValidationSource.LOCAL).getOrThrow()

            verify { CardNetwork.lookupAll(bin) }

            assertEquals(2, result.size)
            assertEquals(CardNetwork.Type.VISA.name.titlecase(), result[0].displayName)
            assertEquals(CardNetwork.Type.MASTERCARD.name.titlecase(), result[1].displayName)
        }

    @Test
    fun `getBinMetadata fetches from local source when ValidationSource is LOCAL_FALLBACK`() =
        runTest {
            mockkObject(CardNetwork.Companion)
            val bin = "123456"
            val cardDescriptors =
                listOf(
                    mockk<CardNetwork.Descriptor> {
                        every { type } returns CardNetwork.Type.VISA
                    },
                    mockk<CardNetwork.Descriptor> {
                        every { type } returns CardNetwork.Type.MASTERCARD
                    },
                )

            every { CardNetwork.lookupAll(bin) } returns cardDescriptors
            val result = repository.getBinMetadata(bin, ValidationSource.LOCAL_FALLBACK).getOrThrow()

            verify { CardNetwork.lookupAll(bin) }

            assertEquals(2, result.size)
            assertEquals(CardNetwork.Type.VISA.name.titlecase(), result[0].displayName)
            assertEquals(CardNetwork.Type.MASTERCARD.name.titlecase(), result[1].displayName)
        }

    private fun String.titlecase(): String {
        if (isNullOrBlank()) return ""
        val sb = StringBuilder(first().titlecase())
        if (length > 1) {
            sb.append(substring(1).lowercase())
        }
        return sb.toString()
    }
}
