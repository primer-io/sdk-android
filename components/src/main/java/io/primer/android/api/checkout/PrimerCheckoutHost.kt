package io.primer.android.api.checkout

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.components.AppBarSpec
import io.primer.android.internal.presentation.checkout.components.FlowSheetOverlayInline
import io.primer.android.internal.presentation.checkout.components.LocalAppBarSpec
import io.primer.android.internal.presentation.checkout.components.LocalIsInlineFlow
import io.primer.android.internal.presentation.checkout.components.LocalPrimerSettings
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController

/**
 * Inline checkout host for embedding payment components in your own layout.
 *
 * Use this when you want full control over the checkout layout. Place SDK
 * components ([io.primer.android.api.components.card.PrimerCardForm], [io.primer.android.api.components.paymentMethods.PrimerPaymentMethods], etc.) directly in your UI.
 * The SDK handles payment flows (3DS, redirects, etc.) via overlay sheets.
 *
 * For a complete modal checkout experience, use [PrimerCheckoutSheet] instead.
 *
 * ## Card form embedded in your screen
 * ```kotlin
 * @Composable
 * fun CheckoutScreen() {
 *     val checkout = rememberPrimerCheckoutState(clientToken) { result -> }
 *
 *     Scaffold(topBar = { MyAppBar() }) { padding ->
 *         PrimerCheckoutHost(checkout, Modifier.padding(padding)) {
 *             Column(Modifier.padding(16.dp)) {
 *                 Text("Enter card details", style = MaterialTheme.typography.h6)
 *                 Spacer(Modifier.height(16.dp))
 *
 *                 val cardFormState = rememberCardFormState(checkout)
 *                 PrimerCardForm(state = cardFormState)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * ## Payment methods with card form below
 * ```kotlin
 * PrimerCheckoutHost(checkout) {
 *     val paymentMethodState = rememberPaymentMethodState(checkout)
 *     val cardFormState = rememberCardFormState(checkout)
 *
 *     Column {
 *         Text("Select payment method")
 *         PrimerPaymentMethodList(paymentMethodState)
 *
 *         Spacer(Modifier.height(24.dp))
 *
 *         Text("Or pay with card")
 *         PrimerCardForm(state = cardFormState)
 *     }
 * }
 * ```
 *
 * ## Custom submit button
 * ```kotlin
 * PrimerCheckoutHost(checkout) {
 *     val state = rememberCardFormState(checkout)
 *     val formState by state.state.collectAsStateWithLifecycle()
 *
 *     PrimerCardForm(
 *         state = state,
 *         submitButton = {
 *             MyBrandButton(
 *                 text = "Complete Purchase",
 *                 enabled = formState.isFormValid && !formState.isLoading,
 *                 onClick = { state.submit() }
 *             )
 *         }
 *     )
 * }
 * ```
 *
 * @param checkout Checkout state from [rememberPrimerCheckoutController]
 * @param modifier Modifier for the host container
 * @param theme Theme customization for colors, typography, and spacing
 * @param content Your layout containing SDK payment components
 */
@Composable
fun PrimerCheckoutHost(
    checkout: PrimerCheckoutController,
    modifier: Modifier = Modifier,
    theme: PrimerTheme = PrimerTheme(),
    content: @Composable () -> Unit,
) {
    val viewModel = checkout as CheckoutViewModel
    val navController = rememberNavController()

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.cleanupActiveFlow()
        }
    }

    CompositionLocalProvider(
        LocalPrimerTheme provides theme,
        LocalSheetNavController provides navController,
        LocalAppBarSpec provides AppBarSpec.Hidden,
        LocalPrimerSettings provides checkout.settings,
        LocalIsInlineFlow provides true,
    ) {
        PrimerTheme {
            Box(modifier = modifier) {
                content()
            }

            FlowSheetOverlayInline(viewModel)
        }
    }
}
