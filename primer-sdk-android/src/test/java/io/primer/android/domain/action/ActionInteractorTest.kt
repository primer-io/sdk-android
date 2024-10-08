package io.primer.android.domain.action

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.ClientSessionDataResponse
import io.primer.android.domain.ClientSessionData
import io.primer.android.domain.action.models.ActionUpdateShippingOptionIdParams
import io.primer.android.domain.action.models.BaseActionUpdateParams
import io.primer.android.domain.action.models.MultipleActionUpdateParams
import io.primer.android.domain.action.models.PrimerClientSession
import io.primer.android.domain.action.repository.ActionRepository
import io.primer.android.domain.action.validator.ActionUpdateFilter
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ActionInteractorTest {
    private val paymentMethodData: ClientSessionDataResponse.PaymentMethodDataResponse = mockk() {
        every { surcharges } returns mapOf("method1" to 100, "method2" to 200)
    }
    private val clientSessionDataResponse: ClientSessionDataResponse = mockk {
        every { paymentMethod } returns paymentMethodData
    }

    private val clientSessionData = ClientSessionData(
        clientSession = PrimerClientSession(
            customerId = "123",
            orderId = "order123",
            currencyCode = "USD",
            totalAmount = 1000,
            lineItems = null,
            orderDetails = null,
            customer = null,
            paymentMethod = null,
            fees = null
        )
    )

    private val emptyClientSession = PrimerClientSession(
        customerId = null,
        orderId = null,
        currencyCode = null,
        totalAmount = null,
        lineItems = null,
        orderDetails = null,
        customer = null,
        paymentMethod = null,
        fees = null
    )

    private lateinit var actionRepository: ActionRepository
    private lateinit var actionUpdateFilter: ActionUpdateFilter
    private lateinit var localConfigurationDataSource: LocalConfigurationDataSource
    private lateinit var errorEventResolver: BaseErrorEventResolver
    private lateinit var eventDispatcher: EventDispatcher
    private lateinit var actionInteractor: ActionInteractor
    private val baseActionParams = mockk<BaseActionUpdateParams>()
    private val multipleActionUpdateParams = MultipleActionUpdateParams(listOf(baseActionParams))

    @BeforeEach
    fun setUp() {
        actionRepository = mockk {
            every { updateClientActions(any()) } returns flowOf(clientSessionData)
        }
        actionUpdateFilter = mockk {
            coEvery { filter(any()) } returns false
        }
        localConfigurationDataSource = mockk()
        errorEventResolver = mockk()
        eventDispatcher = mockk(relaxed = true)
        actionInteractor = ActionInteractor(
            actionRepository,
            actionUpdateFilter,
            localConfigurationDataSource,
            errorEventResolver,
            eventDispatcher,
            Dispatchers.Unconfined
        )
    }

    @Test
    fun `invoke with BaseActionUpdateParams should return empty ClientSessionData when params are invalid`() = runTest {
        val params = mockk<BaseActionUpdateParams>(relaxed = true)
        coEvery { actionUpdateFilter.filter(any()) } returns true

        val result = actionInteractor.invoke(params).single()

        assertEquals(emptyClientSession, result.clientSession)
    }

    @Test
    fun `execute should filter out params that do not pass the filter`() = runTest {
        coEvery { actionUpdateFilter.filter(any()) } returns true

        val result = actionInteractor.execute(multipleActionUpdateParams).single()

        assertEquals(ClientSessionData(emptyClientSession), result)
        coVerify { actionUpdateFilter.filter(baseActionParams) }
        coVerify(exactly = 0) { actionRepository.updateClientActions(any()) }
    }

    @Test
    fun `execute should return empty ClientSessionData when params are empty`() = runTest {
        val emptyParams = MultipleActionUpdateParams(emptyList())

        val result = actionInteractor.execute(emptyParams).single()

        assertEquals(ClientSessionData(emptyClientSession), result)
        coVerify(exactly = 0) { actionUpdateFilter.filter(any()) }
        coVerify(exactly = 0) { actionRepository.updateClientActions(any()) }
    }

    @Test
    fun `execute should return empty ClientSessionData when params are repeated`() = runTest {
        val params = MultipleActionUpdateParams(listOf(ActionUpdateShippingOptionIdParams("id")))

        every { actionRepository.updateClientActions(params.params) } returns flowOf(clientSessionData)

        val firstResult = actionInteractor(params).single()
        val secondResult = actionInteractor(params).single()

        assertEquals(null, secondResult.clientSession.customerId)
        verify(exactly = 1) { eventDispatcher.dispatchEvent(ofType(CheckoutEvent.ClientSessionUpdateStarted::class)) }
    }

    @Test
    fun `execute should return ClientSessionData when params are valid`() = runTest {
        val params = ActionUpdateShippingOptionIdParams("id")

        every { actionRepository.updateClientActions(listOf(params)) } returns flowOf(clientSessionData)

        val result = actionInteractor(params).single()

        assertEquals(clientSessionData, result)
        verify { eventDispatcher.dispatchEvent(ofType(CheckoutEvent.ClientSessionUpdateSuccess::class)) }
    }

    @Test
    fun `execute should handle errors`() = runTest {
        val params = mockk<BaseActionUpdateParams>(relaxed = true)
        val exception = RuntimeException("Test exception")

        every { errorEventResolver.resolve(exception, ErrorMapperType.ACTION_UPDATE) } just Runs
        every { actionRepository.updateClientActions(listOf(params)) } returns flow { throw exception }

        val result = actionInteractor(params).catch { }.toList()

        assertEquals(0, result.size)
        verify { errorEventResolver.resolve(exception, ErrorMapperType.ACTION_UPDATE) }
    }

    @Test
    fun `surcharges should return correct map when configuration contains surcharges`() {
        val surcharges = mapOf("method1" to 100, "method2" to 200)
        every { localConfigurationDataSource.getConfiguration().clientSession } returns clientSessionDataResponse

        assertEquals(surcharges, actionInteractor.surcharges)
    }

    @Test
    fun `surcharges should return empty map when configuration does not contain surcharges`() {
        val clientSession = mockk<ClientSessionDataResponse>(relaxed = true)
        every { localConfigurationDataSource.getConfiguration().clientSession } returns clientSession

        assertTrue(actionInteractor.surcharges.isEmpty())
    }

    @Test
    fun `surchargeDataEmptyOrZero should return true when configuration contains no surcharges`() {
        val clientSession = mockk<ClientSessionDataResponse>(relaxed = true)
        every { localConfigurationDataSource.getConfiguration().clientSession } returns clientSession

        assertTrue(actionInteractor.surchargeDataEmptyOrZero)
    }

    @Test
    fun `surchargeDataEmptyOrZero should return true when configuration contains surcharges with zero values`() {
        val surcharges = mapOf("method1" to 0, "method2" to 0)
        every { paymentMethodData.surcharges } returns surcharges
        every { localConfigurationDataSource.getConfiguration().clientSession } returns clientSessionDataResponse

        assertTrue(actionInteractor.surchargeDataEmptyOrZero)
    }

    @Test
    fun `surchargeDataEmptyOrZero should return false when configuration contains surcharges with non-zero values`() {
        val surcharges = mapOf("method1" to 100, "method2" to 200)
        every { paymentMethodData.surcharges } returns surcharges
        every { localConfigurationDataSource.getConfiguration().clientSession } returns clientSessionDataResponse

        assertFalse(actionInteractor.surchargeDataEmptyOrZero)
    }
}
