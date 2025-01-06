package io.primer.android.nolpay.implementation.listCards.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.primer.android.core.domain.None
import io.primer.android.nolpay.implementation.common.domain.NolPaySdkInitInteractor
import io.primer.android.nolpay.implementation.listCards.domain.NolPayGetLinkedCardsInteractor
import io.primer.android.nolpay.implementation.listCards.domain.model.NolPayGetLinkedCardsParams
import io.primer.android.phoneMetadata.domain.PhoneMetadataInteractor
import io.primer.android.phoneMetadata.domain.model.PhoneMetadata
import io.primer.android.phoneMetadata.domain.model.PhoneMetadataParams
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class NolPayGetLinkedCardsDelegateTest {
    @RelaxedMockK
    private lateinit var getLinkedCardsInteractor: NolPayGetLinkedCardsInteractor

    @RelaxedMockK
    private lateinit var phoneMetadataInteractor: PhoneMetadataInteractor

    @RelaxedMockK
    private lateinit var sdkInitInteractor: NolPaySdkInitInteractor

    private lateinit var delegate: NolPayGetLinkedCardsDelegate

    @BeforeEach
    fun setUp() {
        delegate =
            NolPayGetLinkedCardsDelegate(
                getLinkedCardsInteractor,
                phoneMetadataInteractor,
                sdkInitInteractor,
            )
    }

    @Test
    fun `getLinkedCards should return linked cards when successful`() =
        runTest {
            val mobileNumber = "1234567890"
            val phoneMetadata = PhoneMetadata("1234567890", "US")
            val linkedCards =
                listOf(
                    PrimerNolPaymentCard("card1"),
                    PrimerNolPaymentCard("card2"),
                )

            coEvery { sdkInitInteractor(None) } returns Result.success(Unit)
            coEvery { phoneMetadataInteractor(PhoneMetadataParams(mobileNumber)) } returns Result.success(phoneMetadata)
            coEvery { getLinkedCardsInteractor(any()) } returns Result.success(linkedCards)

            val result = delegate.getLinkedCards(mobileNumber)

            assertTrue(result.isSuccess)
            assertEquals(linkedCards, result.getOrThrow())

            coVerify { sdkInitInteractor(None) }
            coVerify { phoneMetadataInteractor(PhoneMetadataParams(mobileNumber)) }
            coVerify { getLinkedCardsInteractor(any()) }
            coVerify { getLinkedCardsInteractor(any<NolPayGetLinkedCardsParams>()) }
        }

    @Test
    fun `getLinkedCards should return error when phone metadata retrieval fails`() =
        runTest {
            val mobileNumber = "1234567890"
            val expectedException = Exception("Phone metadata retrieval failed")

            coEvery { sdkInitInteractor(None) } returns Result.success(Unit)
            coEvery { phoneMetadataInteractor(PhoneMetadataParams(mobileNumber)) } returns
                Result.failure(expectedException)

            val result = delegate.getLinkedCards(mobileNumber)

            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())

            coVerify { sdkInitInteractor(None) }
            coVerify { phoneMetadataInteractor(PhoneMetadataParams(mobileNumber)) }
            coVerify(exactly = 0) { getLinkedCardsInteractor(any()) }
        }

    @Test
    fun `getLinkedCards should return error when get linked cards fails`() =
        runTest {
            val mobileNumber = "1234567890"
            val phoneMetadata = PhoneMetadata("1234567890", "US")
            val expectedException = Exception("Get linked cards failed")

            coEvery { sdkInitInteractor(None) } returns Result.success(Unit)
            coEvery { phoneMetadataInteractor(PhoneMetadataParams(mobileNumber)) } returns Result.success(phoneMetadata)
            coEvery { getLinkedCardsInteractor(any()) } returns Result.failure(expectedException)

            val result = delegate.getLinkedCards(mobileNumber)

            assertTrue(result.isFailure)
            assertEquals(expectedException, result.exceptionOrNull())

            coVerify { sdkInitInteractor(None) }
            coVerify { phoneMetadataInteractor(PhoneMetadataParams(mobileNumber)) }
            coVerify { getLinkedCardsInteractor(any()) }
        }
}
