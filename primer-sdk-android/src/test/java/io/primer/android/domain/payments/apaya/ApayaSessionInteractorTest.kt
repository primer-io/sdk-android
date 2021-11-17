package io.primer.android.domain.payments.apaya

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor.Companion.RETURN_URL
import io.primer.android.domain.payments.apaya.models.ApayaSession
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.payments.apaya.repository.ApayaRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class ApayaSessionInteractorTest {

    @RelaxedMockK
    internal lateinit var apayaSessionParamsValidator: ApayaSessionParamsValidator

    @RelaxedMockK
    internal lateinit var apayaWebResultValidator: ApayaWebResultValidator

    @RelaxedMockK
    internal lateinit var apayaRepository: ApayaRepository

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var sessionInteractor: ApayaSessionInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sessionInteractor =
            ApayaSessionInteractor(
                apayaSessionParamsValidator,
                apayaWebResultValidator,
                apayaRepository,
                eventDispatcher,
                testCoroutineDispatcher
            )
    }

    @Test
    fun `validateWebResultParams() should continue when WebResultParams validation was success`() {
        val webResultParams = mockk<ApayaWebResultParams>(relaxed = true)
        every { apayaWebResultValidator.validate(any()) }.returns(flowOf(Unit))
        runBlockingTest {
            sessionInteractor.validateWebResultParams(webResultParams).first()
        }
        verify { apayaWebResultValidator.validate(any()) }
    }

    @Test
    fun `validateWebResultParams() should dispatch error events when validation failed`() {
        val webResultParams = mockk<ApayaWebResultParams>(relaxed = true)
        every { apayaWebResultValidator.validate(any()) }.returns(
            flow {
                throw Exception("Validation failed.")
            }
        )

        val event = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                sessionInteractor.validateWebResultParams(webResultParams).first()
            }
        }

        verify { apayaWebResultValidator.validate(any()) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.TOKENIZE_ERROR)
    }

    @Test
    fun `createClientSession() should dispatch error events when ApayaSessionParamsValidator validate() failed`() {
        val sessionParams = mockk<ApayaSessionParams>(relaxed = true)
        every { apayaSessionParamsValidator.validate(any()) }.returns(
            flow {
                throw Exception("Validation failed.")
            }
        )
        val event = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                sessionInteractor(sessionParams).first()
            }
        }

        verify { apayaSessionParamsValidator.validate(any()) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.TOKENIZE_ERROR)
    }

    @Test
    fun `createClientSession() should dispatch error events when ApayaRepository createClientSession() failed`() {
        val sessionParams = mockk<ApayaSessionParams>(relaxed = true)
        every { apayaSessionParamsValidator.validate(any()) }.returns(flowOf(Unit))
        every { apayaRepository.createClientSession(any()) }.returns(
            flow {
                throw Exception("Create session failed.")
            }
        )
        val event = slot<CheckoutEvent>()

        assertThrows<Exception> {
            runBlockingTest {
                sessionInteractor(sessionParams).first()
            }
        }

        verify { apayaSessionParamsValidator.validate(any()) }
        verify { apayaRepository.createClientSession(any()) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.TOKENIZE_ERROR)
    }

    @Test
    fun `createClientSession() should return ApayaPaymentData when ApayaRepository createClientSession() was success`() {
        val sessionParams = mockk<ApayaSessionParams>(relaxed = true)
        val session = mockk<ApayaSession>(relaxed = true)

        every { apayaSessionParamsValidator.validate(any()) }.returns(flowOf(Unit))
        every { apayaRepository.createClientSession(any()) }.returns(flowOf(session))

        runBlockingTest {
            val result = sessionInteractor(sessionParams).first()
            assertEquals(result.token, session.token)
            assertEquals(result.redirectUrl, session.redirectUrl)
            assertEquals(result.returnUrl, RETURN_URL)
        }

        verify { apayaSessionParamsValidator.validate(any()) }
        verify { apayaRepository.createClientSession(any()) }
    }
}
