package io.primer.android.api.checkout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.api.components.card.PrimerCardForm
import io.primer.android.api.components.card.rememberCardFormController
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethods
import io.primer.android.api.components.paymentMethods.PrimerVaultedPaymentMethods
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.components.paymentMethods.rememberVaultedPaymentMethodsController
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.navigation.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.components.AppBarSpec
import io.primer.android.internal.presentation.checkout.components.DefaultError
import io.primer.android.internal.presentation.checkout.components.DefaultLoading
import io.primer.android.internal.presentation.checkout.components.DefaultSplash
import io.primer.android.internal.presentation.checkout.components.DefaultSuccess
import io.primer.android.internal.presentation.checkout.components.LocalAppBarSpec
import io.primer.android.internal.presentation.checkout.components.LocalPrimerSettings
import io.primer.android.internal.presentation.checkout.components.SheetNavHost
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController
import kotlinx.coroutines.delay

/**
 * Modal bottom sheet checkout with built-in navigation.
 *
 * Displays a complete checkout flow in a bottom sheet with:
 * - Payment method selection (with saved methods if available)
 * - Card form for card payments
 * - Native flows for other payment methods (Klarna, PayPal, etc.)
 * - Success and error screens
 *
 * Navigation between screens is handled automatically. Customize individual
 * screens via slot parameters, or use defaults for a complete checkout experience.
 *
 * ## Quick start
 * ```kotlin
 * val checkout = rememberPrimerCheckoutState(clientToken) { result ->
 *     when (result) {
 *         is PrimerResult.Success -> navigateToConfirmation()
 *         is PrimerResult.Failure -> showError(result.error)
 *         else -> { }
 *     }
 * }
 *
 * PrimerCheckoutSheet(checkout)
 * ```
 *
 * ## Custom submit button
 * ```kotlin
 * PrimerCheckoutSheet(
 *     checkout = checkout,
 *     cardForm = {
 *         val state = rememberCardFormState(checkout)
 *         PrimerCardForm(
 *             state = state,
 *             submitButton = {
 *                 val formState by state.state.collectAsStateWithLifecycle()
 *                 MyBrandButton(
 *                     text = "Pay now",
 *                     enabled = formState.isFormValid,
 *                     onClick = { state.submit() }
 *                 )
 *             }
 *         )
 *     }
 * )
 * ```
 *
 * ## Custom payment method items
 * ```kotlin
 * PrimerCheckoutSheet(
 *     checkout = checkout,
 *     paymentMethodSelection = {
 *         PrimerCheckoutSheetDefaults.PaymentMethodSelection(
 *             checkout = checkout,
 *             paymentMethods = {
 *                 val state = rememberPaymentMethodState(checkout)
 *                 PrimerPaymentMethodList(
 *                     state = state,
 *                     item = { method, onClick ->
 *                         MyPaymentMethodCard(
 *                             name = method.paymentMethodName,
 *                             onClick = onClick
 *                         )
 *                     }
 *                 )
 *             }
 *         )
 *     }
 * )
 * ```
 *
 * ## Hide vaulted methods
 * ```kotlin
 * PrimerCheckoutSheet(
 *     checkout = checkout,
 *     paymentMethodSelection = {
 *         PrimerCheckoutSheetDefaults.PaymentMethodSelection(
 *             checkout = checkout,
 *             vaultedMethods = {}, // Empty to hide
 *         )
 *     }
 * )
 * ```
 *
 * @param checkout Checkout state from [rememberPrimerCheckoutController]
 * @param modifier Modifier for the bottom sheet container
 * @param onDismiss Called when the sheet is dismissed (swipe down or back press)
 * @param theme Theme customization for colors, typography, and spacing
 * @param splash Content shown as initial splash before checkout loads
 * @param loading Content shown while checkout is initializing
 * @param paymentMethodSelection Content for the payment method selection screen
 * @param cardForm Content for the card payment form screen
 * @param success Content shown after successful payment
 * @param error Content shown when payment fails
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimerCheckoutSheet(
    checkout: PrimerCheckoutController,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    theme: PrimerTheme = PrimerTheme(),
    splash: @Composable () -> Unit = { PrimerCheckoutSheetDefaults.Splash() },
    loading: @Composable () -> Unit = { PrimerCheckoutSheetDefaults.Loading() },
    paymentMethodSelection: @Composable () -> Unit = {
        PrimerCheckoutSheetDefaults.PaymentMethodSelection(checkout)
    },
    cardForm: @Composable () -> Unit = {
        val cardFormController = rememberCardFormController(checkout)
        PrimerCardForm(controller = cardFormController)
    },
    success: @Composable (PrimerCheckoutData) -> Unit = { data ->
        PrimerCheckoutSheetDefaults.Success(checkoutData = data)
    },
    error: @Composable (PrimerError) -> Unit = { err ->
        PrimerCheckoutSheetDefaults.Error(error = err)
    },
) {
    val viewModel = checkout as CheckoutViewModel
    val swipeEnabled = checkout.isSwipeEnabled

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { if (!swipeEnabled) it != SheetValue.Hidden else true },
    )
    val navController = rememberNavController()
    var showSheet by remember { mutableStateOf(true) }

    // Observe navigation events for dismiss handling
    LaunchedEffect(viewModel.navigator) {
        viewModel.navigator.navigationEvents.collect { event ->
            when (event) {
                is CheckoutNavigator.NavigationEvent.NavigateToSuccess -> {
                    delay(CheckoutNavigator.SUCCESS_SCREEN_DELAY_MS)
                    viewModel.navigator.signalDismissAfterResult()
                }

                is CheckoutNavigator.NavigationEvent.DismissAfterResult,
                is CheckoutNavigator.NavigationEvent.Dismiss,
                -> {
                    showSheet = false
                    onDismiss()
                }

                else -> {
                    // Handled by SheetNavHost
                }
            }
        }
    }

    if (showSheet) {
        CompositionLocalProvider(
            LocalPrimerTheme provides theme,
            LocalSheetNavController provides navController,
            LocalAppBarSpec provides AppBarSpec.Hidden,
            LocalPrimerSettings provides viewModel.settings,
        ) {
            PrimerTheme {
                ModalBottomSheet(
                    onDismissRequest = {
                        showSheet = false
                        onDismiss()
                    },
                    modifier = modifier,
                    sheetState = sheetState,
                    dragHandle = null,
                    shape = RoundedCornerShape(
                        topStart = theme.radiusTokens.large,
                        topEnd = theme.radiusTokens.large,
                    ),
                    containerColor = theme.colorTokens().primerColorBackground,
                ) {
                    // Cleanup active payment flow when modal content leaves composition
                    DisposableEffect(viewModel) {
                        onDispose {
                            viewModel.cleanupActiveFlow()
                        }
                    }

                    SheetNavHost(
                        checkout = viewModel,
                        navController = navController,
                        splash = splash,
                        loading = loading,
                        paymentMethodSelection = paymentMethodSelection,
                        cardForm = cardForm,
                        success = success,
                        error = error,
                    )
                }
            }
        }
    }
}

/**
 * Default screen implementations for [PrimerCheckoutSheet].
 *
 * Use these as building blocks when customizing individual screens while
 * keeping others at their defaults.
 */
object PrimerCheckoutSheetDefaults {

    /**
     * Default splash screen.
     */
    @Composable
    fun Splash() {
        DefaultSplash()
    }

    /**
     * Default loading screen with centered spinner.
     */
    @Composable
    fun Loading() {
        DefaultLoading()
    }

    /**
     * Default success screen with checkmark animation.
     *
     * @param checkoutData Checkout data from successful payment (for displaying order info)
     * @param title Custom title text (defaults to standard success message)
     * @param message Custom message text (defaults to standard success message)
     */
    @Composable
    fun Success(
        checkoutData: PrimerCheckoutData,
        title: String? = null,
        message: String? = null,
    ) {
        DefaultSuccess(
            checkoutData = checkoutData,
            title = title,
            message = message,
        )
    }

    /**
     * Default error screen with retry option.
     *
     * @param error The error from the failed payment (for displaying error details)
     * @param title Custom title text (defaults to standard error message)
     * @param message Custom message text (defaults to error description)
     */
    @Composable
    fun Error(
        error: PrimerError,
        title: String? = null,
        message: String? = null,
    ) {
        DefaultError(
            error = error,
            title = title,
            message = message,
        )
    }

    /**
     * Default payment method selection screen.
     *
     * Shows saved payment methods (if customer has any) followed by
     * available payment methods for selection.
     *
     * @param checkout Checkout state from [rememberPrimerCheckoutController]
     * @param vaultedMethods Slot for saved/vaulted payment methods section
     * @param paymentMethods Slot for available payment methods list
     */
    @Composable
    fun PaymentMethodSelection(
        checkout: PrimerCheckoutController,
        vaultedMethods: @Composable () -> Unit = { VaultedMethods(checkout) },
        paymentMethods: @Composable () -> Unit = { PaymentMethods(checkout) },
    ) {
        val spacingTokens = LocalPrimerTheme.current.spacingTokens

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacingTokens.large),
        ) {
            vaultedMethods()
            paymentMethods()
        }
    }

    /**
     * Default vaulted/saved payment methods section.
     *
     * Shows saved payment methods with a "Pay" button. Hidden automatically
     * if the customer has no saved payment methods.
     */
    @Composable
    fun VaultedMethods(checkout: PrimerCheckoutController) {
        val vaultedState = rememberVaultedPaymentMethodsController(checkout)
        val methods by vaultedState.methods.collectAsStateWithLifecycle()
        val spacingTokens = LocalPrimerTheme.current.spacingTokens

        if (methods.isNotEmpty()) {
            PrimerVaultedPaymentMethods(vaultedState)
            Spacer(modifier = Modifier.height(spacingTokens.large))
        }
    }

    /**
     * Default available payment methods list.
     *
     * Shows all available payment methods (cards, PayPal, etc.) for selection.
     */
    @Composable
    fun PaymentMethods(checkout: PrimerCheckoutController) {
        val paymentMethodState = rememberPaymentMethodsController(checkout)
        PrimerPaymentMethods(paymentMethodState)
    }
}
