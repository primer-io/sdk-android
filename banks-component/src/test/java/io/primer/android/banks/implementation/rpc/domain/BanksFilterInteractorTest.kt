package io.primer.android.banks.implementation.rpc.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.InstantExecutorExtension
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankFilterParams
import io.primer.android.banks.implementation.rpc.domain.repository.IssuingBankRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class BanksFilterInteractorTest {

    @RelaxedMockK
    internal lateinit var repository: IssuingBankRepository

    private lateinit var interactor: BanksFilterInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor = BanksFilterInteractor(repository)
    }

    @Test
    fun `filterIssuingBanks() should return list of sorted and filtered banks when IssuingBankRepository getCachedIssuingBanks() was success`() {
        val filter = "u"
        val params = IssuingBankFilterParams(filter)
        coEvery { repository.getCachedIssuingBanks() } returns Result.success(banks)
        runTest {
            val res = interactor(params).getOrThrow()
            assertEquals(
                banks.sortedBy { it.name.lowercase() }
                    .filter { it.name.contains(filter) },
                res
            )
        }

        coVerify { repository.getCachedIssuingBanks() }
    }

    @Test
    fun `filterIssuingBanks() should return exception when IssuingBankRepository getCachedIssuingBanks() was failure`() {
        val exception = mockk<Exception>()
        val filter = "u"
        val params = IssuingBankFilterParams(filter)
        coEvery { repository.getCachedIssuingBanks() } returns Result.failure(exception)
        runTest {
            val res = interactor(params).exceptionOrNull()
            assertEquals(exception, res)
        }

        coVerify { repository.getCachedIssuingBanks() }
    }

    private companion object {
        val banks = listOf(
            IssuingBank("1", "bunq", false, ""),
            IssuingBank("2", "Revolut", false, ""),
            IssuingBank("3", "Abn amro", false, "")
        )
    }
}
