package io.primer.android.components.manager.redirect.component

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.components.manager.redirect.composable.WebRedirectStep
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.webRedirectShared.implementation.composer.presentation.delegate.WebRedirectDelegate
import io.primer.android.webRedirectShared.implementation.composer.presentation.delegate.WebRedirectLoggingDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.time.Duration.Companion.seconds

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class WebRedirectComponentTest {
    private val paymentMethodType = "paymentMethodType"
    private val errors = listOf(mockk<PrimerError>(), mockk<PrimerError>())
    private val steps =
        listOf(WebRedirectStep.Loaded, WebRedirectStep.Success, WebRedirectStep.Dismissed)

    @MockK
    private lateinit var webRedirectDelegate: WebRedirectDelegate

    @MockK
    private lateinit var webRedirectLoggingDelegate: WebRedirectLoggingDelegate

    private lateinit var component: WebRedirectComponent

    @BeforeEach
    fun setUp() {
        every { webRedirectDelegate.errors() } returns flowOf(*errors.toTypedArray())
        every { webRedirectDelegate.steps() } returns flowOf(*steps.toTypedArray())
        coEvery { webRedirectLoggingDelegate.logError(any(), any()) } just Runs
        coEvery { webRedirectLoggingDelegate.logStep(any(), any()) } just Runs
        component =
            WebRedirectComponent(
                paymentMethodType,
                webRedirectDelegate,
                webRedirectLoggingDelegate,
            )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(webRedirectDelegate, webRedirectLoggingDelegate)
    }

    @Test
    fun `start() should log errors and steps via logging delegate when called`() =
        runTest {
            component.start()

            delay(1.seconds)

            verify(exactly = 1) {
                webRedirectDelegate.errors()
                webRedirectDelegate.steps()
            }
            coVerify(exactly = 1) {
                errors.forEach {
                    webRedirectLoggingDelegate.logError(
                        error = it,
                        paymentMethodType = paymentMethodType,
                    )
                }
                (listOf(WebRedirectStep.Loading) + steps).forEach {
                    webRedirectLoggingDelegate.logStep(
                        webRedirectStep = it,
                        paymentMethodType = paymentMethodType,
                    )
                }
            }
        }
}
