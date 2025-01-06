package io.primer.android.banks.implementation.rpc.presentation.delegate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.banks.implementation.configuration.domain.BankIssuerConfigurationInteractor
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfig
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfigParams
import io.primer.android.banks.implementation.rpc.domain.BanksFilterInteractor
import io.primer.android.banks.implementation.rpc.domain.BanksInteractor
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBank
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankFilterParams
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Locale
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class GetBanksDelegateTest {
    private val paymentMethodType = "paymentMethodType"

    private val paymentMethodConfigId = "paymentMethodConfigId"

    private val locale = mockk<Locale>()

    private val bankIssuerConfig: BankIssuerConfig =
        mockk {
            every { this@mockk.paymentMethodConfigId } returns this@GetBanksDelegateTest.paymentMethodConfigId
            every { this@mockk.locale } returns this@GetBanksDelegateTest.locale
        }

    @MockK
    private lateinit var banks: List<IssuingBank>

    @MockK
    private lateinit var banksInteractor: BanksInteractor

    @MockK
    private lateinit var banksFilterInteractor: BanksFilterInteractor

    @MockK
    private lateinit var configurationInteractor: BankIssuerConfigurationInteractor

    private lateinit var delegate: GetBanksDelegate

    @BeforeEach
    fun setUp() {
        delegate =
            GetBanksDelegate(
                paymentMethodType = paymentMethodType,
                banksInteractor = banksInteractor,
                banksFilterInteractor = banksFilterInteractor,
                configurationInteractor = configurationInteractor,
            )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(banksInteractor, banksFilterInteractor, configurationInteractor)
    }

    @Test
    fun `getBanks() should return list of banks returned by banks interactor when query is null`() =
        runTest {
            coEvery {
                configurationInteractor(any())
            } returns Result.success(bankIssuerConfig)
            coEvery { banksInteractor(any()) } returns Result.success(banks)

            val result = delegate.getBanks(query = null)

            assertEquals(banks, result.getOrThrow())
            coVerify(exactly = 1) {
                banksInteractor(
                    IssuingBankParams(
                        paymentMethodConfigId = paymentMethodConfigId,
                        paymentMethod = paymentMethodType,
                        locale = locale,
                    ),
                )
            }
            coVerify(exactly = 1) {
                configurationInteractor(
                    BankIssuerConfigParams(paymentMethodType = paymentMethodType),
                )
            }
        }

    @Test
    fun `getBanks() should return exception thrown by banks interactor when interactor fails`() =
        runTest {
            val exception = Exception()
            coEvery {
                configurationInteractor(any())
            } returns Result.success(bankIssuerConfig)
            coEvery { banksInteractor(any()) } returns Result.failure(exception)

            val result = delegate.getBanks(query = null)

            assertEquals(exception, result.exceptionOrNull())
            coVerify(exactly = 1) {
                banksInteractor(
                    IssuingBankParams(
                        paymentMethodConfigId = paymentMethodConfigId,
                        paymentMethod = paymentMethodType,
                        locale = locale,
                    ),
                )
            }
            coVerify(exactly = 1) {
                configurationInteractor(
                    BankIssuerConfigParams(paymentMethodType = paymentMethodType),
                )
            }
        }

    @Test
    fun `getBanks() should return exception thrown by payment method modules interactor when interactor fails`() =
        runTest {
            val exception = Exception()
            coEvery {
                configurationInteractor(any())
            } returns Result.failure(exception)

            val result = delegate.getBanks(query = null)

            assertEquals(exception, result.exceptionOrNull())
            coVerify(exactly = 1) {
                configurationInteractor(
                    BankIssuerConfigParams(paymentMethodType = paymentMethodType),
                )
            }
        }

    @Test
    fun `getBanks() should return list of banks returned by banks filter interactor when query is not null`() =
        runTest {
            coEvery { banksFilterInteractor(any()) } returns Result.success(banks)

            val result = delegate.getBanks(query = "query")

            assertEquals(banks, result.getOrThrow())
            coVerify(exactly = 1) {
                banksFilterInteractor(
                    IssuingBankFilterParams(text = "query"),
                )
            }
        }

    @Test
    fun `getBanks()should return exception thrown by banks filter interactor when interactor fails`() =
        runTest {
            val exception = Exception()
            coEvery { banksFilterInteractor(any()) } returns Result.failure(exception)

            val result = delegate.getBanks(query = "query")

            assertEquals(exception, result.exceptionOrNull())
            coVerify(exactly = 1) {
                banksFilterInteractor(
                    IssuingBankFilterParams(text = "query"),
                )
            }
        }
}
