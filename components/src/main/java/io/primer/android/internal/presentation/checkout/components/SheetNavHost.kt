package io.primer.android.internal.presentation.checkout.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.primer.android.LocalPrimerTheme
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.components.R
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.PrimerCheckoutData
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.internal.navigation.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController
import io.primer.android.internal.presentation.checkout.navigation.vaultNavGraph
import io.primer.android.internal.presentation.screens.country.CountrySelectionScreen
import io.primer.android.internal.presentation.screens.cvvRecapture.CvvRecaptureScreen
import io.primer.android.internal.presentation.screens.klarna.Klarna
import io.primer.android.internal.presentation.screens.nativeUi.NativeUiPaymentMethodViewModel
import io.primer.android.internal.presentation.screens.nativeUi.NativeUiPaymentMethodViewModelFactory
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.serialization.Serializable

internal val LocalPrimerSettings = staticCompositionLocalOf<PrimerSettings> {
    error("PrimerSettings not provided.")
}

/**
 * Composition local for dismiss action.
 * Used by CloseButton and other components that need to dismiss the checkout.
 */
internal val LocalDismissAction = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Navigation host for PrimerCheckout in sheet mode.
 *
 * Manages navigation between checkout screens:
 * - Loading screen (during initialization)
 * - Payment methods screen (main content)
 * - Card form screen
 * - Country selection screen
 * - CVV recapture screen (for vaulted card payments requiring CVV)
 * - Vault management screens (manage, delete confirmation)
 * - Native UI screens (for APMs like PayPal, Klarna)
 * - Success/Error screens
 *
 * Each screen can be customized via slot parameters.
 */
@Suppress("LongMethod")
@Composable
internal fun SheetNavHost(
    checkout: CheckoutViewModel,
    navController: NavHostController,
    splash: @Composable () -> Unit,
    loading: @Composable () -> Unit,
    paymentMethodSelection: @Composable () -> Unit,
    cardForm: @Composable () -> Unit,
    success: @Composable (PrimerCheckoutData) -> Unit,
    error: @Composable (PrimerError) -> Unit,
) {
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    val isLoading = checkoutState is PrimerCheckoutState.Loading

    // Handle init events
    LaunchedEffect(checkout) {
        checkout.initEvent.collect { event ->
            when (event) {
                is CheckoutViewModel.InitEvent.Ready -> {
                    navController.navigate(Screen.Content) {
                        popUpTo(Screen.Loading) { inclusive = true }
                    }
                }

                is CheckoutViewModel.InitEvent.Error -> {
                    navController.navigate(Screen.Error) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
        }
    }

    // Handle country navigation requests
    LaunchedEffect(checkout.countryNavigator) {
        checkout.countryNavigator.navigationRequests.collect {
            navController.navigate(Screen.CountrySelection)
        }
    }

    // Handle navigation events from navigator
    LaunchedEffect(checkout.navigator) {
        checkout.navigator.navigationEvents.collect { event ->
            when (event) {
                is CheckoutNavigator.NavigationEvent.NavigateToCardForm -> {
                    navController.navigate(Screen.CardForm)
                }

                is CheckoutNavigator.NavigationEvent.StartPaymentFlow -> {
                    when (PaymentMethodType.safeValueOf(event.paymentMethodType)) {
                        PaymentMethodType.KLARNA -> navController.navigate(Screen.Klarna)
                        else -> navController.navigate(Screen.NativeUi(event.paymentMethodType))
                    }
                }

                is CheckoutNavigator.NavigationEvent.StartVaultedPaymentFlow -> {
                    // Check if CVV is required - will emit ShowCvvRecapture or proceed to payment
                    checkout.checkCvvAndStartVaultedPayment(event.vaultedMethod)
                }

                is CheckoutNavigator.NavigationEvent.NavigateToSuccess -> {
                    navController.navigate(Screen.Success) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                is CheckoutNavigator.NavigationEvent.NavigateToError -> {
                    navController.navigate(Screen.Error) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                is CheckoutNavigator.NavigationEvent.NavigateToVaultManage -> {
                    navController.navigate(Screen.Vault.Manage) {
                        launchSingleTop = true
                    }
                }

                is CheckoutNavigator.NavigationEvent.ShowCvvRecapture -> {
                    navController.navigate(Screen.CvvRecapture) {
                        launchSingleTop = true
                    }
                }

                is CheckoutNavigator.NavigationEvent.NavigateToLoading -> {
                    navController.navigate(Screen.Loading) {
                        launchSingleTop = true
                    }
                }

                is CheckoutNavigator.NavigationEvent.Dismiss,
                is CheckoutNavigator.NavigationEvent.DismissAfterResult,
                -> {
                    // Handled by PrimerCheckoutSheet
                }
            }
        }
    }

    // Actions for error screen
    val retryAction = {
        checkout.refresh()
    }
    val otherMethodsAction: () -> Unit = {
        navController.navigate(Screen.Content) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
    val dismissAction = { checkout.navigator.dismiss() }

    CompositionLocalProvider(
        LocalSheetNavController provides navController,
        LocalDismissAction provides dismissAction,
        LocalRetryAction provides retryAction,
        LocalOtherMethodsAction provides otherMethodsAction,
    ) {
        CheckoutNavContent(
            checkout = checkout,
            navController = navController,
            startDestination = if (isLoading) Screen.Splash else Screen.Content,
            splash = splash,
            loading = loading,
            paymentMethodSelection = paymentMethodSelection,
            cardForm = cardForm,
            success = success,
            error = error,
        )
    }
}

/**
 * Shared navigation content that can be embedded in sheet or modal.
 *
 * Contains all checkout screens with proper navigation handling.
 * Used by SheetNavHost for full sheet mode and FlowSheetOverlayInline for modal sub-flows.
 *
 * @param checkout The checkout ViewModel for state and navigation
 * @param navController Navigation controller for screen transitions
 * @param startDestination The initial screen to display
 * @param loading Composable for loading screen
 * @param paymentMethodSelection Composable for payment method selection screen
 * @param cardForm Composable for card form
 * @param success Composable for success screen
 * @param error Composable for error screen
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
internal fun CheckoutNavContent(
    checkout: CheckoutViewModel,
    navController: NavHostController,
    startDestination: Screen,
    splash: @Composable () -> Unit = { DefaultSplash() },
    loading: @Composable () -> Unit = { DefaultLoading() },
    paymentMethodSelection: @Composable () -> Unit = {},
    cardForm: @Composable () -> Unit = {},
    success: @Composable (PrimerCheckoutData) -> Unit = { data -> DefaultSuccess(checkoutData = data) },
    error: @Composable (PrimerError) -> Unit = { err -> DefaultError(error = err) },
) {
    // Get terminal state data from main checkout state
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    val successData = (checkoutState as? PrimerCheckoutState.Success)?.checkoutData
    val errorData = (checkoutState as? PrimerCheckoutState.Failure)?.error

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.animateContentSize(animationSpec = tween()),
        enterTransition = { fadeIn(animationSpec = tween()) },
        exitTransition = { fadeOut(animationSpec = tween()) },
        popEnterTransition = { fadeIn(animationSpec = tween()) },
        popExitTransition = { fadeOut(animationSpec = tween()) },
    ) {
        composable<Screen.Splash> {
            WithAppBar(AppBarSpec.Hidden) {
                splash()
            }
        }

        composable<Screen.Loading> {
            WithAppBar(AppBarSpec.Standard(navigation = Navigation.None)) {
                loading()
            }
        }

        composable<Screen.Content> {
            val checkoutState by checkout.state.collectAsStateWithLifecycle()
            var cachedFormattedAmount by remember { mutableStateOf<String?>(null) }

            // Cache formatted amount from Ready state to avoid crashes when state transitions
            LaunchedEffect(checkoutState) {
                if (checkoutState is PrimerCheckoutState.Ready) {
                    val totalAmount = (checkoutState as PrimerCheckoutState.Ready).clientSession.totalAmount ?: 0
                    cachedFormattedAmount = checkout.formatAmount(totalAmount)
                }
            }

            WithAppBar(
                AppBarSpec.Standard(
                    title = stringResource(R.string.primer_common_button_pay_amount, cachedFormattedAmount ?: ""),
                ),
            ) {
                paymentMethodSelection()
            }
        }

        composable<Screen.CardForm> {
            val payButtonAddNewCard = LocalPrimerSettings.current.uiOptions.cardFormUIOptions.payButtonAddNewCard
            val titleRes = if (payButtonAddNewCard) {
                R.string.primer_common_add_card
            } else {
                R.string.primer_card_form_title
            }
            WithAppBar(
                AppBarSpec.Standard(
                    title = stringResource(titleRes),
                ),
            ) {
                val spacing = LocalPrimerTheme.current.spacingTokens
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(spacing.large),
                ) {
                    cardForm()
                }
            }
        }

        composable<Screen.CountrySelection> {
            WithAppBar(
                AppBarSpec.Standard(
                    title = stringResource(R.string.primer_country_title),
                ),
            ) {
                CountrySelectionScreen(navController)
            }
        }

        composable<Screen.Klarna> {
            WithAppBar(
                AppBarSpec.Standard(
                    title = stringResource(R.string.primer_klarna_title),
                    titleAlignment = TitleAlignment.Center,
                ),
            ) {
                KlarnaScreen(checkout)
            }
        }

        composable<Screen.NativeUi> { backStackEntry ->
            val screen = backStackEntry.toRoute<Screen.NativeUi>()
            WithAppBar(AppBarSpec.Standard(actions = Actions.Auto)) {
                NativeUiScreen(
                    paymentMethodType = screen.paymentMethodType,
                    checkout = checkout,
                )
            }
        }

        composable<Screen.Success> {
            WithAppBar(AppBarSpec.Standard(navigation = Navigation.None, actions = Actions.None)) {
                successData?.let { success(it) }
            }
        }

        composable<Screen.Error> {
            WithAppBar(AppBarSpec.Standard(navigation = Navigation.None, actions = Actions.None)) {
                errorData?.let { error(it) }
            }
        }

        composable<Screen.CvvRecapture> {
            CvvRecaptureScreen(checkout)
        }

        vaultNavGraph(checkout)
    }
}

/**
 * Uses Kotlin serialization for type-safe navigation.
 */
@Serializable
internal sealed interface Screen {

    @Serializable
    data object Splash : Screen

    @Serializable
    data object Loading : Screen

    @Serializable
    data object Content : Screen

    @Serializable
    data object CardForm : Screen

    @Serializable
    data class NativeUi(val paymentMethodType: String) : Screen

    @Serializable
    data object CountrySelection : Screen

    @Serializable
    data object Klarna : Screen

    @Serializable
    data object Success : Screen

    @Serializable
    data object Error : Screen

    /**
     * Top-level CVV recapture screen for vaulted card payments.
     * Used when paying with a saved card that requires CVV re-entry.
     * Separate from vault management for cleaner navigation (can be used as startDestination).
     */
    @Serializable
    data object CvvRecapture : Screen

    @Serializable
    data object Vault : Screen {

        @Serializable
        data object SelectedMethod : Screen

        @Serializable
        data object Manage : Screen

        @Serializable
        data object DeleteConfirmation : Screen
    }
}

/**
 * Shared content for Native UI payment flows (APMs like PayPal, Google Pay, etc.).
 *
 * Creates and manages NativeUiPaymentMethodViewModel, reports results via navigator.
 * Navigation to Success/Error screens is handled by SheetNavHost via navigator events.
 *
 * Used by both SheetNavHost (for sheet mode) and FlowSheetOverlayInline (for inline mode).
 *
 * @param paymentMethodType The payment method type (e.g., "PAYPAL", "GOOGLE_PAY")
 * @param checkout The checkout ViewModel for registering cleanup
 */
@Composable
internal fun NativeUiPaymentContent(
    paymentMethodType: String,
    checkout: CheckoutViewModel,
) {
    val viewModel = viewModel<NativeUiPaymentMethodViewModel>(
        key = paymentMethodType,
        factory = NativeUiPaymentMethodViewModelFactory(
            paymentMethodType = paymentMethodType,
        ),
    )

    // Register this flow as the active cleanable
    LaunchedEffect(viewModel) {
        checkout.setActiveFlow(viewModel)
    }

    // Handle navigation events from the native UI ViewModel
    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { event ->
            when (event) {
                is NativeUiPaymentMethodViewModel.NavigationEvent.PaymentSuccess -> {
                    checkout.navigator.onSuccess(event.checkoutData)
                }

                is NativeUiPaymentMethodViewModel.NavigationEvent.PaymentError -> {
                    checkout.navigator.onError(event.error)
                }
            }
        }
    }

    // Show loading while processing payment
    DefaultLoading()
}

/**
 * Native UI screen for APM payment flows in sheet mode.
 * Navigation is handled by SheetNavHost via navigator events.
 */
@Composable
private fun NativeUiScreen(
    paymentMethodType: String,
    checkout: CheckoutViewModel,
) {
    NativeUiPaymentContent(
        paymentMethodType = paymentMethodType,
        checkout = checkout,
    )
}

/**
 * Klarna screen for V2 navigation.
 * Uses the new V2 Klarna API which handles navigation internally.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun KlarnaScreen(checkout: CheckoutViewModel) {
    Klarna(checkout = checkout)
}
