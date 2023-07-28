package io.primer.android.domain.payments.apaya

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.apaya.ApayaSessionInteractor.Companion.RETURN_URL
import io.primer.android.domain.payments.apaya.models.ApayaSession
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import io.primer.android.domain.payments.apaya.models.ApayaWebResultParams
import io.primer.android.domain.payments.apaya.repository.ApayaSessionRepository
import io.primer.android.domain.payments.apaya.validation.ApayaSessionParamsValidator
import io.primer.android.domain.payments.apaya.validation.ApayaWebResultValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ApayaSessionInteractorTest {

    @RelaxedMockK
    internal lateinit var apayaSessionParamsValidator: ApayaSessionParamsValidator

    @RelaxedMockK
    internal lateinit var apayaWebResultValidator: ApayaWebResultValidator

    @RelaxedMockK
    internal lateinit var apayaSessionRepository: ApayaSessionRepository

    @RelaxedMockK
    internal lateinit var errorEventResolver: CheckoutErrorEventResolver

    private lateinit var sessionInteractor: ApayaSessionInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        sessionInteractor =
            ApayaSessionInteractor(
                apayaSessionParamsValidator,
                apayaWebResultValidator,
                apayaSessionRepository,
                errorEventResolver,
            )
    }

    @Test
    fun `validateWebResultParams() should continue when WebResultParams validation was success`() {
        val webResultParams = mockk<ApayaWebResultParams>(relaxed = true)
        coEvery { apayaWebResultValidator.validate(any()) }.returns(flowOf(Unit))
        runTest {
            sessionInteractor.validateWebResultParams(webResultParams).first()
        }

        coVerify { apayaWebResultValidator.validate(any()) }
    }

    @Test
    fun `validateWebResultParams() should dispatch error events when validation failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Validation failed.")

        val webResultParams = mockk<ApayaWebResultParams>(relaxed = true)
        coEvery { apayaWebResultValidator.validate(any()) }.returns(
            flow {
                throw exception
            }
        )

        val event = slot<Exception>()

        assertThrows<Exception> {
            runTest {
                sessionInteractor.validateWebResultParams(webResultParams).first()
            }
        }

        coVerify { apayaWebResultValidator.validate(any()) }
        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.SESSION_CREATE) }

        assertEquals(exception.javaClass, event.captured.javaClass)
    }

    @Test
    fun `createClientSession() should dispatch error events when ApayaSessionParamsValidator validate() failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Validation failed.")

        val sessionParams = mockk<ApayaSessionParams>(relaxed = true)
        coEvery { apayaSessionParamsValidator.validate(any()) }.returns(
            flow {
                throw exception
            }
        )
        val event = slot<Exception>()

        assertThrows<Exception> {
            runTest {
                sessionInteractor(sessionParams).first()
            }
        }

        coVerify { apayaSessionParamsValidator.validate(any()) }
        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.SESSION_CREATE) }

        assertEquals(exception.javaClass, event.captured.javaClass)
    }

    @Test
    fun `createClientSession() should dispatch error events when ApayaRepository createClientSession() failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Create session failed.")

        val sessionParams = mockk<ApayaSessionParams>(relaxed = true)
        coEvery { apayaSessionParamsValidator.validate(any()) }.returns(flowOf(Unit))
        coEvery { apayaSessionRepository.createClientSession(any()) }.returns(
            flow {
                throw exception
            }
        )
        val event = slot<Throwable>()

        assertThrows<Exception> {
            runTest {
                sessionInteractor(sessionParams).first()
            }
        }

        coVerify { apayaSessionParamsValidator.validate(any()) }
        coVerify { apayaSessionRepository.createClientSession(any()) }
        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.SESSION_CREATE) }

        assertEquals(exception.javaClass, event.captured.javaClass)
    }

    @Test
    fun `createClientSession() should return ApayaPaymentData when ApayaRepository createClientSession() was success`() {
        val sessionParams = mockk<ApayaSessionParams>(relaxed = true)
        val session = mockk<ApayaSession>(relaxed = true)

        coEvery { apayaSessionParamsValidator.validate(any()) }.returns(flowOf(Unit))
        coEvery { apayaSessionRepository.createClientSession(any()) }.returns(flowOf(session))

        runTest {
            val result = sessionInteractor(sessionParams).first()
            assertEquals(result.token, session.token)
            assertEquals(result.redirectUrl, session.redirectUrl)
            assertEquals(result.returnUrl, RETURN_URL)
        }

        coVerify { apayaSessionParamsValidator.validate(any()) }
        coVerify { apayaSessionRepository.createClientSession(any()) }
    }
}
