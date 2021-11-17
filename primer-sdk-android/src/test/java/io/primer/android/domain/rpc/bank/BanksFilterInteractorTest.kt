package io.primer.android.domain.rpc.bank

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankFilterParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class BanksFilterInteractorTest {

    @RelaxedMockK
    internal lateinit var repository: IssuingBankRepository

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var interactor: BanksFilterInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            BanksFilterInteractor(
                repository,
                testCoroutineDispatcher
            )
    }

    @Test
    fun `filterIssuingBanks() should return list of sorted and filtered banks IssuingBankRepository getCachedIssuingBanks() was success`() {
        val filter = "u"
        val params = IssuingBankFilterParams(filter)
        every { repository.getCachedIssuingBanks() }.returns(flowOf(banks))
        testCoroutineDispatcher.runBlockingTest {
            val res = interactor(params).first()
            Assertions.assertEquals(
                banks.sortedBy { it.name.lowercase() }
                    .filter { it.name.contains(filter) },
                res
            )
        }

        verify { repository.getCachedIssuingBanks() }
    }

    private companion object {
        val banks = listOf(
            IssuingBank("1", "bunq", false, ""),
            IssuingBank("2", "Revolut", false, ""),
            IssuingBank("3", "Abn amro", false, ""),
        )
    }
}
