package io.primer.android.internal.presentation.screens.klarna

import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.LocalPrimerTheme
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.screens.klarna.components.KlarnaAuthorizeButton
import io.primer.android.internal.presentation.screens.klarna.components.KlarnaCategoryItem
import io.primer.android.internal.presentation.screens.klarna.components.KlarnaFinalizeButton
import io.primer.android.internal.presentation.screens.klarna.components.KlarnaLoading
import io.primer.android.scope.PrimerKlarnaScope

/** Klarna payment method component. Displays Klarna payment categories and handles the payment flow. */
@Composable
internal fun Klarna(
    checkout: PrimerCheckoutController,
    modifier: Modifier = Modifier,
    categoryItem: @Composable (PrimerKlarnaScope.Category, Boolean, Boolean, View?, () -> Unit) -> Unit =
        { category, isSelected, isLoading, paymentView, onClick ->
            KlarnaDefaults.CategoryItem(category, isSelected, isLoading, paymentView, onClick)
        },
    authorizeButton: @Composable (Boolean, Boolean, () -> Unit) -> Unit = { enabled, loading, onClick ->
        KlarnaDefaults.AuthorizeButton(enabled, loading, onClick)
    },
    finalizeButton: @Composable (Boolean, Boolean, () -> Unit) -> Unit = { enabled, loading, onClick ->
        KlarnaDefaults.FinalizeButton(enabled, loading, onClick)
    },
) {
    val checkoutVm = checkout as CheckoutViewModel
    val viewModel = viewModel<KlarnaViewModel>(factory = KlarnaViewModelFactory(LocalViewModelStoreOwner.current!!))
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Register this flow as the active cleanable
    LaunchedEffect(viewModel) {
        checkoutVm.setActiveFlow(viewModel)
    }

    LaunchedEffect(viewModel) {
        viewModel.navigation.collect { event ->
            when (event) {
                is KlarnaViewModel.NavigationEvent.Success -> checkoutVm.navigator.onSuccess(event.checkoutData)
                is KlarnaViewModel.NavigationEvent.Error -> checkoutVm.navigator.onError(event.error)
            }
        }
    }

    Column(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 300.dp),
    ) {
        KlarnaStepContent(state, viewModel, categoryItem, authorizeButton, finalizeButton)
    }
}

@Composable
private fun KlarnaStepContent(
    state: PrimerKlarnaScope.State,
    viewModel: KlarnaViewModel,
    categoryItem: @Composable (PrimerKlarnaScope.Category, Boolean, Boolean, View?, () -> Unit) -> Unit,
    authorizeButton: @Composable (Boolean, Boolean, () -> Unit) -> Unit,
    finalizeButton: @Composable (Boolean, Boolean, () -> Unit) -> Unit,
) {
    when (state.step) {
        PrimerKlarnaScope.Step.Loading -> {
            if (state.categories.isEmpty()) {
                KlarnaDefaults.Loading()
            } else {
                KlarnaCategoryList(state, viewModel, categoryItem, isLoadingCategory = true)
            }
        }
        PrimerKlarnaScope.Step.CategorySelection -> KlarnaCategoryList(state, viewModel, categoryItem, false)
        PrimerKlarnaScope.Step.ViewReady -> {
            KlarnaCategoryList(state, viewModel, categoryItem, isLoadingCategory = false)
            Spacer(Modifier.height(LocalPrimerTheme.current.spacingTokens.large))
            authorizeButton(true, false) { viewModel.authorizePayment() }
        }
        PrimerKlarnaScope.Step.AuthorizationStarted -> KlarnaDefaults.Loading()
        PrimerKlarnaScope.Step.AwaitingFinalization -> finalizeButton(true, false) { viewModel.finalizePayment() }
    }
}

@Composable
private fun KlarnaCategoryList(
    state: PrimerKlarnaScope.State,
    viewModel: KlarnaViewModel,
    categoryItem: @Composable (PrimerKlarnaScope.Category, Boolean, Boolean, View?, () -> Unit) -> Unit,
    isLoadingCategory: Boolean,
) {
    val spacing = LocalPrimerTheme.current.spacingTokens
    val context = LocalContext.current

    Column(Modifier.fillMaxWidth().padding(horizontal = spacing.large), Arrangement.spacedBy(spacing.medium)) {
        state.categories.forEach { category ->
            val isSelected = category.id == state.selectedCategoryId
            val paymentView = if (isSelected && state.step == PrimerKlarnaScope.Step.ViewReady) {
                state.paymentView?.get()
            } else {
                null
            }
            categoryItem(category, isSelected, isSelected && isLoadingCategory, paymentView) {
                viewModel.selectPaymentCategory(context, category.id)
            }
        }
    }
}

/**
 * Default component implementations for [Klarna].
 */
internal object KlarnaDefaults {

    /**
     * Loading indicator shown while Klarna initializes.
     */
    @Composable
    fun Loading(modifier: Modifier = Modifier) {
        KlarnaLoading(modifier)
    }

    /**
     * Payment category card with selection state and embedded payment view.
     */
    @Composable
    fun CategoryItem(
        category: PrimerKlarnaScope.Category,
        isSelected: Boolean,
        isLoading: Boolean,
        paymentView: View?,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        KlarnaCategoryItem(category, isSelected, isLoading, paymentView, onClick, modifier)
    }

    /**
     * Authorize button to continue with Klarna payment.
     */
    @Composable
    fun AuthorizeButton(
        enabled: Boolean,
        isLoading: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        KlarnaAuthorizeButton(enabled, isLoading, onClick, modifier)
    }

    /**
     * Finalize button to complete Klarna payment.
     */
    @Composable
    fun FinalizeButton(
        enabled: Boolean,
        isLoading: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        KlarnaFinalizeButton(enabled, isLoading, onClick, modifier)
    }
}
