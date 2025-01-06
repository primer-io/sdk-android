package io.primer.android.domain.action

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.clientSessionActions.domain.ActionInteractor
import io.primer.android.clientSessionActions.domain.DefaultActionInteractor
import io.primer.android.clientSessionActions.domain.handlers.CheckoutClientSessionActionsHandler
import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.clientSessionActions.domain.models.MultipleActionUpdateParams
import io.primer.android.clientSessionActions.domain.repository.ActionRepository
import io.primer.android.clientSessionActions.domain.validator.ActionUpdateFilter
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.domain.model.ClientSessionData
import io.primer.android.configuration.domain.repository.ConfigurationRepository
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.errors.domain.BaseErrorResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ActionInteractorTest {
    private val paymentMethodData: ClientSessionDataResponse.PaymentMethodDataResponse =
        mockk {
            every { surcharges } returns mapOf("method1" to 100, "method2" to 200)
        }
    private val clientSessionDataResponse: ClientSessionDataResponse =
        mockk {
            every { paymentMethod } returns paymentMethodData
        }

    private val clientSessionData =
        ClientSessionData(
            clientSession =
                PrimerClientSession(
                    customerId = "123",
                    orderId = "order123",
                    currencyCode = "USD",
                    totalAmount = 1000,
                    lineItems = null,
                    orderDetails = null,
                    customer = null,
                    paymentMethod = null,
                    fees = null,
                ),
        )

    private val emptyClientSession =
        PrimerClientSession(
            customerId = null,
            orderId = null,
            currencyCode = null,
            totalAmount = null,
            lineItems = null,
            orderDetails = null,
            customer = null,
            paymentMethod = null,
            fees = null,
        )

    private lateinit var actionRepository: ActionRepository
    private lateinit var actionUpdateFilter: ActionUpdateFilter
    private lateinit var localConfigurationDataSource: CacheConfigurationDataSource
    private lateinit var configurationRepository: ConfigurationRepository
    private lateinit var errorEventResolver: BaseErrorResolver
    private lateinit var clientSessionActionsHandler: CheckoutClientSessionActionsHandler
    private lateinit var actionInteractor: ActionInteractor
    private val baseActionParams = mockk<BaseActionUpdateParams>()
    private val multipleActionUpdateParams = MultipleActionUpdateParams(listOf(baseActionParams))

    @BeforeEach
    fun setUp() {
        actionRepository = mockk()
        actionUpdateFilter =
            mockk {
                coEvery { filter(any()) } returns false
            }
        localConfigurationDataSource = mockk()
        errorEventResolver = mockk()
        configurationRepository = mockk()
        errorEventResolver = mockk()
        clientSessionActionsHandler = mockk()
        actionInteractor =
            DefaultActionInteractor(
                actionRepository = actionRepository,
                configurationRepository = configurationRepository,
                actionUpdateFilter = actionUpdateFilter,
                errorEventResolver = errorEventResolver,
                clientSessionActionsHandler = clientSessionActionsHandler,
                dispatcher = Dispatchers.Unconfined,
            )
    }

    @Test
    fun `invoke with BaseActionUpdateParams should return ClientSessionData from configuration when param list is empty`() =
        runTest {
            val primerClientSession = mockk<PrimerClientSession>()
            every { configurationRepository.getConfiguration() } returns
                mockk {
                    every {
                        clientSession.clientSessionDataResponse.toClientSessionData().clientSession
                    } returns primerClientSession
                }
            coEvery { actionUpdateFilter.filter(any()) } returns false

            val result = actionInteractor.invoke(mockk { every { params } returns emptyList() }).getOrThrow()

            assertEquals(primerClientSession, result.clientSession)
            verify {
                configurationRepository.getConfiguration()
            }
            verify(exactly = 0) {
                clientSessionActionsHandler.onClientSessionUpdateStarted()
                clientSessionActionsHandler.onClientSessionUpdateSuccess(any())
                errorEventResolver.resolve(any())
                clientSessionActionsHandler.onClientSessionUpdateError(any())
            }
            coVerify(exactly = 0) {
                actionRepository.updateClientActions(any())
            }
        }

    @Test
    fun `invoke with BaseActionUpdateParams should return ClientSessionData from configuration when param is repeated`() =
        runTest {
            val primerClientSession = mockk<PrimerClientSession>()
            every { configurationRepository.getConfiguration() } returns
                mockk {
                    every {
                        clientSession.clientSessionDataResponse.toClientSessionData().clientSession
                    } returns primerClientSession
                }
            coEvery { actionUpdateFilter.filter(any()) } returns false
            every { clientSessionActionsHandler.onClientSessionUpdateStarted() } just Runs
            coEvery { actionRepository.updateClientActions(any()) } returns Result.success(clientSessionData)
            every { clientSessionActionsHandler.onClientSessionUpdateSuccess(any()) } just Runs
            val params =
                mockk<MultipleActionUpdateParams> {
                    every { params } returns listOf(mockk())
                }

            val firstResult = actionInteractor(params).getOrThrow()
            val secondResult = actionInteractor(params).getOrThrow()

            assertEquals(clientSessionData.clientSession, firstResult.clientSession)
            assertEquals(primerClientSession, secondResult.clientSession)
            verify {
                configurationRepository.getConfiguration()
            }
            verify(exactly = 1) {
                clientSessionActionsHandler.onClientSessionUpdateStarted()
                clientSessionActionsHandler.onClientSessionUpdateSuccess(any())
                configurationRepository.getConfiguration()
            }
            verify(exactly = 2) {
                actionUpdateFilter.filter(any())
            }
            verify(exactly = 0) {
                errorEventResolver.resolve(any())
                clientSessionActionsHandler.onClientSessionUpdateError(any())
            }
            coVerify(exactly = 1) {
                actionRepository.updateClientActions(any())
            }
        }

    @Test
    fun `invoke should handle errors`() =
        runTest {
            val params =
                mockk<MultipleActionUpdateParams> {
                    every { params } returns listOf(mockk())
                }
            val exception = RuntimeException("Test exception")
            every { actionUpdateFilter.filter(any()) } returns false
            coEvery { clientSessionActionsHandler.onClientSessionUpdateStarted() } just Runs
            coEvery { actionRepository.updateClientActions(any()) } returns Result.failure(exception)
            val error = mockk<PrimerError>()
            every { errorEventResolver.resolve(exception) } returns error
            coEvery { clientSessionActionsHandler.onClientSessionUpdateError(any()) } just Runs

            val result = actionInteractor(params).getOrNull()

            assertNull(result)
            coVerify {
                actionRepository.updateClientActions(any())
            }
            verify {
                clientSessionActionsHandler.onClientSessionUpdateStarted()
                errorEventResolver.resolve(exception)
                clientSessionActionsHandler.onClientSessionUpdateError(any())
            }
            verify(exactly = 0) {
                configurationRepository.getConfiguration()
                clientSessionActionsHandler.onClientSessionUpdateSuccess(any())
            }
        }
}
