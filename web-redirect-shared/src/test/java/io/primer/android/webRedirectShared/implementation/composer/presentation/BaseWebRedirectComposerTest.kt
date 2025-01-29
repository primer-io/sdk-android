package io.primer.android.webRedirectShared.implementation.composer.presentation

import android.app.Activity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.paymentmethods.core.composer.composable.ComposerUiEvent
import io.primer.android.webRedirectShared.implementation.composer.ui.navigation.launcher.WebRedirectActivityLauncherParams
import io.primer.paymentMethodCoreUi.core.ui.navigation.launchers.PaymentMethodLauncherParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class BaseWebRedirectComposerTest {
    private val dispatcher = TestCoroutineDispatcher()
    private val scope = TestCoroutineScope(dispatcher)

    private lateinit var uiEvent: MutableSharedFlow<ComposerUiEvent>
    private lateinit var composer: BaseWebRedirectComposer

    @BeforeEach
    fun setUp() {
        uiEvent = MutableSharedFlow()
        composer =
            spyk(
                object : BaseWebRedirectComposer {
                    override val scope: CoroutineScope = this@BaseWebRedirectComposerTest.scope

                    override val _uiEvent: MutableSharedFlow<ComposerUiEvent> = this@BaseWebRedirectComposerTest.uiEvent

                    override fun onResultCancelled(params: WebRedirectLauncherParams) {
                        println("onResultCancelled")
                    }

                    override fun onResultOk(params: WebRedirectLauncherParams) {
                        println("onResultOk")
                    }

                    override fun cancel() {
                        // no - op
                    }
                },
            )
    }

    @Test
    fun `handleActivityResultIntent should call onResultCancelled on result canceled`() {
        val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
        val webRedirectParams = mockk<WebRedirectLauncherParams>(relaxed = true)
        every { params.initialLauncherParams } returns webRedirectParams

        composer.handleActivityResultIntent(params, Activity.RESULT_CANCELED, null)

        verify { composer.onResultCancelled(webRedirectParams) }
    }

    @Test
    fun `handleActivityResultIntent should call onResultOk on result ok`() =
        runTest {
            val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
            val webRedirectParams = mockk<WebRedirectLauncherParams>(relaxed = true)
            every { params.initialLauncherParams } returns webRedirectParams

            composer.handleActivityResultIntent(params, Activity.RESULT_OK, null)

            verify { composer.onResultOk(webRedirectParams) }
        }

    @Test
    fun `handleActivityResultIntent should call close`() =
        runTest {
            val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
            val webRedirectParams = mockk<WebRedirectLauncherParams>(relaxed = true)
            every { params.initialLauncherParams } returns webRedirectParams

            val closeSlot = slot<ComposerUiEvent.Finish>()
            val closeFlow = MutableSharedFlow<ComposerUiEvent>()
            coEvery { composer._uiEvent.emit(capture(closeSlot)) } coAnswers { closeFlow.emit(closeSlot.captured) }

            composer.handleActivityResultIntent(params, Activity.RESULT_OK, null)

            coVerify { composer._uiEvent.emit(ComposerUiEvent.Finish) }
        }

    @Test
    fun `handleActivityStartEvent should call openRedirectScreen`() =
        runTest {
            val params = mockk<PaymentMethodLauncherParams>(relaxed = true)
            val webRedirectParams = mockk<WebRedirectLauncherParams>(relaxed = true)
            every { params.initialLauncherParams } returns webRedirectParams

            val openSlot = slot<ComposerUiEvent.Navigate>()
            val openFlow = MutableSharedFlow<ComposerUiEvent>()
            coEvery { composer._uiEvent.emit(capture(openSlot)) } coAnswers { openFlow.emit(openSlot.captured) }

            composer.handleActivityStartEvent(params)

            coVerify {
                composer._uiEvent.emit(
                    ComposerUiEvent.Navigate(
                        WebRedirectActivityLauncherParams(
                            webRedirectParams.statusUrl,
                            webRedirectParams.redirectUrl,
                            webRedirectParams.title,
                            webRedirectParams.paymentMethodType,
                            webRedirectParams.returnUrl,
                        ),
                    ),
                )
            }
        }

    @Test
    fun `close should emit Finish event`() =
        runTest {
            val closeSlot = slot<ComposerUiEvent.Finish>()
            val closeFlow = MutableSharedFlow<ComposerUiEvent>()
            coEvery { composer._uiEvent.emit(capture(closeSlot)) } coAnswers { closeFlow.emit(closeSlot.captured) }

            composer.close()

            coVerify { composer._uiEvent.emit(ComposerUiEvent.Finish) }
        }
}
