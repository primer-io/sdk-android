package io.primer.android.api.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.components.analytics.di.ComponentsAnalyticsContainer
import io.primer.android.components.di.DISdkContextInitializer
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.core.di.DISdkContext
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.internal.di.ComponentsContainer
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.CheckoutViewModelFactory
import io.primer.android.internal.presentation.checkout.IntegrationTypeDetector
import java.util.UUID

/**
 * Creates and remembers the checkout session controller.
 *
 * This is the entry point for Primer checkout. Create the controller first, then choose
 * how to present it:
 * - [PrimerCheckoutSheet] - Modal bottom sheet with built-in navigation
 * - [PrimerCheckoutHost] - Inline components for custom layouts
 *
 * ## Quick start
 * ```kotlin
 * val checkout = rememberPrimerCheckoutController(
 *     clientToken = clientToken,
 *     settings = settings,
 * )
 * val state by checkout.state.collectAsState()
 *
 * when (state) {
 *     is PrimerCheckoutState.Loading -> CircularProgressIndicator()
 *     is PrimerCheckoutState.Ready -> PrimerCheckoutSheet(checkout)
 *     is PrimerCheckoutState.Success -> navigateToConfirmation(state.checkoutData)
 *     is PrimerCheckoutState.Failure -> showError(state.error)
 *     is PrimerCheckoutState.Cancelled -> navigateBack()
 *     is PrimerCheckoutState.TokenCreated -> sendToServer(state.token)
 * }
 * ```
 *
 * ## Inline layout
 * ```kotlin
 * val checkout = rememberPrimerCheckoutController(clientToken, settings)
 * val state by checkout.state.collectAsState()
 *
 * when (state) {
 *     is PrimerCheckoutState.Ready -> {
 *         PrimerCheckoutHost(checkout) {
 *             val cardFormState = rememberCardFormState(checkout)
 *             PrimerCardForm(state = cardFormState)
 *         }
 *     }
 *     is PrimerCheckoutState.Success -> SuccessScreen(state.checkoutData)
 *     // ... handle other states
 * }
 * ```
 *
 * ## MANUAL payment flow
 * For server-side payment processing, configure MANUAL handling:
 * ```kotlin
 * val checkout = rememberPrimerCheckoutController(
 *     clientToken = clientToken,
 *     settings = PrimerSettings().apply {
 *         paymentHandling = PrimerPaymentHandling.MANUAL
 *     },
 * )
 * val state by checkout.state.collectAsState()
 *
 * LaunchedEffect(state) {
 *     when (val currentState = state) {
 *         is PrimerCheckoutState.TokenCreated -> {
 *             // Send token to your server
 *             val success = api.createPayment(currentState.token).await()
 *             checkout.resume(
 *                 if (success) PrimerResumeDecision.Success
 *                 else PrimerResumeDecision.Failure("Payment failed")
 *             )
 *         }
 *         else -> { /* handle other states */ }
 *     }
 * }
 * ```
 *
 * @param clientToken Client token obtained from your server via Primer API
 * @param settings Checkout configuration (payment handling, UI options, etc.)
 * @return Checkout controller with state flow to observe
 */
@ExperimentalPrimerApi
@Composable
fun rememberPrimerCheckoutController(
    clientToken: String,
    settings: PrimerSettings = PrimerSettings(),
): PrimerCheckoutController {
    val context = LocalContext.current
    val view = LocalView.current

    val key = rememberSaveable { UUID.randomUUID().toString() }
    val viewModel = viewModel<CheckoutViewModel>(
        factory = remember(clientToken, settings) {
            val integrationType = IntegrationTypeDetector.detectIntegrationType(view)

            DISdkContextInitializer.initComponents(
                config = PrimerConfig().apply {
                    this.settings = settings
                    this.clientTokenBase64 = clientToken
                },
                context = context,
            )
            DISdkContext.componentsSdkContainer?.apply {
                registerContainer(ComponentsContainer { DISdkContext.container() })
                registerContainer(
                    ComponentsAnalyticsContainer(
                        sdk = { DISdkContext.container() },
                        integrationType = integrationType,
                    ),
                )
            }
            CheckoutViewModelFactory()
        },
        key = key,
    )

    LaunchedEffect(Unit) {
        val logReporter = DISdkContext.container().resolve<LogReporter>()
        logReporter.info("Checkout composable initialized", component = TAG)
    }

    return viewModel
}

private const val TAG = "Checkout"
