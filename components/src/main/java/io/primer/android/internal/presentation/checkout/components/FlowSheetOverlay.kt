package io.primer.android.internal.presentation.checkout.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.api.components.card.PrimerCardForm
import io.primer.android.api.components.card.rememberCardFormController
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.navigation.CheckoutNavigator
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FlowSheetOverlayInline(
    checkout: CheckoutViewModel,
) {
    val theme = LocalPrimerTheme.current
    val settings = LocalPrimerSettings.current
    val isInlineFlow = LocalIsInlineFlow.current

    val flowSheetState = remember { mutableStateOf(FlowSheetState()) }
    var modalNavController by remember { mutableStateOf<NavHostController?>(null) }

    LaunchedEffect(checkout.countryNavigator) {
        checkout.countryNavigator.navigationRequests.collect {
            openOrNavigate(
                state = flowSheetState,
                navController = modalNavController,
                destination = Screen.CountrySelection,
            )
        }
    }

    LaunchedEffect(checkout.countryNavigator) {
        checkout.countryNavigator.countrySelectionResult.collect {
            val popped = modalNavController?.popBackStack() ?: false
            if (!popped) {
                flowSheetState.value = FlowSheetState()
                modalNavController = null
            }
        }
    }

    LaunchedEffect(checkout.navigator) {
        checkout.navigator.navigationEvents.collect { event ->
            handleNavigationEvent(
                event = event,
                checkout = checkout,
                state = flowSheetState,
                navController = modalNavController,
            )
        }
    }

    if (flowSheetState.value.isVisible && flowSheetState.value.startDestination != null) {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = {
                if (!checkout.isSwipeEnabled) it != SheetValue.Hidden else true
            },
        )

        val navController = rememberNavController()

        LaunchedEffect(navController) {
            modalNavController = navController
        }

        ModalBottomSheet(
            onDismissRequest = {
                flowSheetState.value = FlowSheetState()
                modalNavController = null
            },
            sheetState = sheetState,
            dragHandle = null,
            shape = RoundedCornerShape(
                topStart = theme.radiusTokens.large,
                topEnd = theme.radiusTokens.large,
            ),
            containerColor = theme.colorTokens().primerColorBackground,
        ) {
            // Cleanup active payment flow when overlay modal leaves composition
            DisposableEffect(checkout) {
                onDispose {
                    checkout.cleanupActiveFlow()
                }
            }

            FlowSheetContent(
                checkout = checkout,
                navController = navController,
                startDestination = flowSheetState.value.startDestination!!,
                isInlineFlow = isInlineFlow,
                theme = theme,
                settings = settings,
            )
        }
    }
}

private data class FlowSheetState(
    val isVisible: Boolean = false,
    val startDestination: Screen? = null,
)

private fun openOrNavigate(
    state: MutableState<FlowSheetState>,
    navController: NavHostController?,
    destination: Screen,
) {
    if (state.value.isVisible && navController != null) {
        navController.navigate(destination)
    } else {
        state.value = FlowSheetState(
            isVisible = true,
            startDestination = destination,
        )
    }
}

private fun handleNavigationEvent(
    event: CheckoutNavigator.NavigationEvent,
    checkout: CheckoutViewModel,
    state: MutableState<FlowSheetState>,
    navController: NavHostController?,
) {
    when (event) {
        is CheckoutNavigator.NavigationEvent.NavigateToVaultManage ->
            openOrNavigate(state, navController, Screen.Vault)

        is CheckoutNavigator.NavigationEvent.NavigateToCardForm ->
            openOrNavigate(state, navController, Screen.CardForm)

        is CheckoutNavigator.NavigationEvent.ShowCvvRecapture ->
            openOrNavigate(state, navController, Screen.CvvRecapture)

        is CheckoutNavigator.NavigationEvent.NavigateToLoading ->
            openOrNavigate(state, navController, Screen.Loading)

        is CheckoutNavigator.NavigationEvent.StartPaymentFlow -> {
            val destination = when (
                PaymentMethodType.safeValueOf(event.paymentMethodType)
            ) {
                PaymentMethodType.KLARNA -> Screen.Klarna
                else -> Screen.NativeUi(event.paymentMethodType)
            }
            openOrNavigate(state, navController, destination)
        }

        is CheckoutNavigator.NavigationEvent.StartVaultedPaymentFlow -> {
            if (!state.value.isVisible) {
                state.value = FlowSheetState(true, Screen.Loading)
            }
            checkout.checkCvvAndStartVaultedPayment(event.vaultedMethod)
        }

        is CheckoutNavigator.NavigationEvent.NavigateToSuccess,
        is CheckoutNavigator.NavigationEvent.NavigateToError,
        -> {
            state.value = FlowSheetState()
        }

        is CheckoutNavigator.NavigationEvent.DismissAfterResult,
        is CheckoutNavigator.NavigationEvent.Dismiss,
        -> {
            state.value = FlowSheetState()
        }
    }
}

@Composable
private fun FlowSheetContent(
    checkout: CheckoutViewModel,
    navController: NavHostController,
    startDestination: Screen,
    isInlineFlow: Boolean,
    theme: PrimerTheme,
    settings: PrimerSettings,
) {
    val dismissAction = { checkout.navigator.dismiss() }
    val retryAction = {
        checkout.refresh()
    }
    // In inline flow, don't show "other methods" button (null hides it)
    val otherMethodsAction: (() -> Unit)? = null

    CompositionLocalProvider(
        LocalSheetNavController provides navController,
        LocalIsInlineFlow provides isInlineFlow,
        LocalPrimerTheme provides theme,
        LocalPrimerSettings provides settings,
        LocalDismissAction provides dismissAction,
        LocalRetryAction provides retryAction,
        LocalOtherMethodsAction provides otherMethodsAction,
    ) {
        CheckoutNavContent(
            checkout = checkout,
            navController = navController,
            startDestination = startDestination,
            loading = { DefaultLoading() },
            paymentMethodSelection = {},
            cardForm = { CardFormContentInModal(checkout) },
            success = { data -> DefaultSuccess(checkoutData = data) },
            error = { err -> DefaultError(error = err) },
        )
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun CardFormContentInModal(
    checkout: CheckoutViewModel,
) {
    val cardFormController = rememberCardFormController(checkout)
    PrimerCardForm(controller = cardFormController)
}
