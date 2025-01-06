package io.primer.android.phoneMetadata.domain

import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.phoneMetadata.domain.exception.PhoneValidationException
import io.primer.android.phoneMetadata.domain.model.PhoneMetadata
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PhoneMetadataInteractorTest {
    @RelaxedMockK
    lateinit var phoneMetadataRepository: PhoneMetadataRepository

    private lateinit var interactor: PhoneMetadataInteractor

    @BeforeEach
    fun setUp() {
        interactor = PhoneMetadataInteractor(phoneMetadataRepository)
    }

    @Test
    fun `execute should return success when PhoneMetadataRepository getPhoneMetadata is successful`() {
        val params = mockk<PhoneMetadataParams>(relaxed = true)
        val phoneMetadata = mockk<PhoneMetadata>(relaxed = true)
        coEvery { phoneMetadataRepository.getPhoneMetadata(any()) }.returns(
            Result.success(
                phoneMetadata,
            ),
        )

        runTest {
            val result = interactor(params)
            assert(result.isSuccess)
            assertEquals(phoneMetadata, result.getOrThrow())
        }
    }

    @Test
    fun `execute should return exception when PhoneMetadataRepository getPhoneMetadata failed`() {
        val params = mockk<PhoneMetadataParams>(relaxed = true)
        val expectedException = mockk<PhoneValidationException>(relaxed = true)
        coEvery { phoneMetadataRepository.getPhoneMetadata(any()) }.returns(
            Result.failure(
                expectedException,
            ),
        )
        val exception =
            assertThrows<PhoneValidationException> {
                runTest {
                    val result = interactor(params)
                    assert(result.isFailure)
                    result.getOrThrow()
                }
            }

        assertEquals(expectedException, exception)
    }
}
