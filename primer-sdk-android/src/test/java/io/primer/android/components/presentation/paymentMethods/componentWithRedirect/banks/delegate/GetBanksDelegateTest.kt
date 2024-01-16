package io.primer.android.components.presentation.paymentMethods.componentWithRedirect.banks.delegate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.domain.payments.methods.PaymentMethodModulesInteractor
import io.primer.android.domain.rpc.banks.BanksFilterInteractor
import io.primer.android.domain.rpc.banks.BanksInteractor
import io.primer.android.domain.rpc.banks.models.IssuingBank
import io.primer.android.domain.rpc.banks.models.IssuingBankFilterParams
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import io.primer.android.payment.PaymentMethodDescriptor
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

    private val paymentMethodDescriptors = listOf<PaymentMethodDescriptor>(
        mockk {
            every { config.id } returns paymentMethodConfigId
            every { config.type } returns paymentMethodType
            every { localConfig.settings.locale } returns locale
        }
    )

    @MockK
    private lateinit var banks: List<IssuingBank>

    @MockK
    private lateinit var banksInteractor: BanksInteractor

    @MockK
    private lateinit var banksFilterInteractor: BanksFilterInteractor

    @MockK
    private lateinit var paymentMethodModulesInteractor: PaymentMethodModulesInteractor

    private lateinit var delegate: GetBanksDelegate

    @BeforeEach
    fun setUp() {
        delegate = GetBanksDelegate(
            paymentMethodType = paymentMethodType,
            banksInteractor = banksInteractor,
            banksFilterInteractor = banksFilterInteractor,
            paymentMethodModulesInteractor = paymentMethodModulesInteractor
        )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(banksInteractor, banksFilterInteractor, paymentMethodModulesInteractor)
    }

    @Test
    fun `getBanks() should return list of banks returned by banks interactor when query is null`() =
        runTest {
            every {
                paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            } returns paymentMethodDescriptors
            coEvery { banksInteractor(any()) } returns Result.success(banks)

            val result = delegate.getBanks(query = null)

            assertEquals(banks, result.getOrThrow())
            coVerify(exactly = 1) {
                banksInteractor(
                    IssuingBankParams(
                        paymentMethodConfigId = paymentMethodConfigId,
                        paymentMethod = paymentMethodType,
                        locale = locale
                    )
                )
            }
            verify(exactly = 1) {
                paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            }
        }

    @Test
    fun `getBanks() should return exception thrown by banks interactor when interactor fails`() =
        runTest {
            val exception = Exception()
            every {
                paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            } returns paymentMethodDescriptors
            coEvery { banksInteractor(any()) } returns Result.failure(exception)

            val result = delegate.getBanks(query = null)

            assertEquals(exception, result.exceptionOrNull())
            coVerify(exactly = 1) {
                banksInteractor(
                    IssuingBankParams(
                        paymentMethodConfigId = paymentMethodConfigId,
                        paymentMethod = paymentMethodType,
                        locale = locale
                    )
                )
            }
            verify(exactly = 1) {
                paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            }
        }

    @Test
    fun `getBanks() should return exception thrown by payment method modules interactor when interactor fails`() =
        runTest {
            val exception = Exception()
            every {
                paymentMethodModulesInteractor.getPaymentMethodDescriptors()
            } throws exception

            val result = delegate.getBanks(query = null)

            assertEquals(exception, result.exceptionOrNull())
            verify(exactly = 1) {
                paymentMethodModulesInteractor.getPaymentMethodDescriptors()
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
                    IssuingBankFilterParams(text = "query")
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
                    IssuingBankFilterParams(text = "query")
                )
            }
        }
}
