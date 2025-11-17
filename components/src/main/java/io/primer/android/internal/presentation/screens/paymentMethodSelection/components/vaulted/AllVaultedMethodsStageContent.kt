package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.screens.paymentMethodSelection.VaultedPaymentMethodSelectionViewModel
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import io.primer.android.scope.PrimerVaultedScope
import kotlinx.coroutines.launch

/**
 * Modal bottom sheet for managing all saved payment methods.
 * Shows a list of all vaulted payment methods with:
 * - View mode: Allows selection (returns to Selection stage after selection)
 * - Edit mode: Allows deletion with confirmation dialog
 * - Swipe between modes via Edit button in top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrimerPaymentMethodSelectionScope.AllVaultedMethodsStageContent(
    vaultedScope: PrimerVaultedScope,
    viewModel: VaultedPaymentMethodSelectionViewModel,
    onDismiss: () -> Unit,
) {
    val state by vaultedScope.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                viewModel.returnToSelection()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null,
        shape = RoundedCornerShape(
            topStart = LocalPrimerTheme.current.radiusTokens.large,
            topEnd = LocalPrimerTheme.current.radiusTokens.large,
        ),
    ) {
        AllVaultedMethodsContent(
            state = state,
            viewModel = viewModel,
            onDismiss = onDismiss,
            sheetState = sheetState,
            coroutineScope = coroutineScope,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllVaultedMethodsContent(
    state: PrimerVaultedScope.State,
    viewModel: VaultedPaymentMethodSelectionViewModel,
    onDismiss: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    val theme = LocalPrimerTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = theme.spacingTokens.large),
    ) {
        AllVaultedMethodsHeader(state, viewModel, onDismiss, sheetState)
        AllVaultedMethodsTitle()
        AllVaultedMethodsBody(state, viewModel, sheetState, coroutineScope)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllVaultedMethodsHeader(
    state: PrimerVaultedScope.State,
    viewModel: VaultedPaymentMethodSelectionViewModel,
    onDismiss: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
) {
    val theme = LocalPrimerTheme.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = theme.spacingTokens.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = {
                coroutineScope.launch {
                    sheetState.hide()
                    viewModel.returnToSelection()
                    onDismiss()
                }
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_chevron_left),
                contentDescription = null,
                tint = theme.colorTokens().primerColorTextPrimary,
            )
            Text(
                text = stringResource(R.string.primer_components_content_description_back),
                style = theme.typographyTokens.titleLarge.toTextStyle(),
                color = theme.colorTokens().primerColorTextPrimary,
            )
        }

        TitleBarEditButton(viewModel, state)
    }
}

@Composable
private fun TitleBarEditButton(viewModel: VaultedPaymentMethodSelectionViewModel, state: PrimerVaultedScope.State) {
    val theme = LocalPrimerTheme.current
    TextButton(
        onClick = { viewModel.toggleEditMode() },
        enabled = !state.isDeleting,
    ) {
        AnimatedContent(
            targetState = state.editMode,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "editButtonTransition",
        ) { editMode ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.xxsmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (editMode) {
                    PrimerVaultedScope.State.EditMode.View -> {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = theme.colorTokens().primerColorTextPrimary,
                        )
                        Text(
                            text = stringResource(R.string.primer_components_vaulted_edit_button),
                            style = theme.typographyTokens.titleLarge.toTextStyle(),
                            color = theme.colorTokens().primerColorTextPrimary,
                        )
                    }
                    PrimerVaultedScope.State.EditMode.Edit -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_primer_check),
                            contentDescription = null,
                            tint = theme.colorTokens().primerColorTextSecondary,
                        )
                        Text(
                            text = stringResource(R.string.primer_components_vaulted_done_button),
                            style = theme.typographyTokens.titleLarge.toTextStyle(),
                            color = theme.colorTokens().primerColorTextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllVaultedMethodsTitle() {
    val theme = LocalPrimerTheme.current
    Text(
        text = stringResource(R.string.primer_components_vaulted_all_methods_title),
        style = theme.typographyTokens.titleLarge.toTextStyle(),
        color = theme.colorTokens().primerColorTextPrimary,
        modifier = Modifier
            .padding(horizontal = theme.spacingTokens.large)
            .padding(
                top = theme.spacingTokens.medium,
                bottom = theme.spacingTokens.xlarge,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllVaultedMethodsBody(
    state: PrimerVaultedScope.State,
    viewModel: VaultedPaymentMethodSelectionViewModel,
    sheetState: androidx.compose.material3.SheetState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    when {
        state.deletingPaymentMethodId != null -> {
            DeleteConfirmationSection(state, viewModel)
        }
        state.isLoading -> AllVaultedLoadingState()
        state.paymentMethods.isEmpty() -> EmptyStateContent()
        else -> PaymentMethodsListContent(state, viewModel, sheetState, coroutineScope)
    }
}

@Composable
private fun AllVaultedLoadingState() {
    val theme = LocalPrimerTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = theme.colorTokens().primerColorBrand,
        )
    }
}

@Composable
private fun DeleteConfirmationSection(
    state: PrimerVaultedScope.State,
    viewModel: VaultedPaymentMethodSelectionViewModel,
) {
    val paymentMethod = state.paymentMethods.find { it.id == state.deletingPaymentMethodId }
    if (paymentMethod != null) {
        DeleteConfirmationContent(
            paymentMethod = paymentMethod,
            isDeleting = state.isDeleting,
            onCancel = { viewModel.cancelDelete() },
            onConfirm = { viewModel.confirmDelete() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodsListContent(
    state: PrimerVaultedScope.State,
    viewModel: VaultedPaymentMethodSelectionViewModel,
    sheetState: androidx.compose.material3.SheetState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    val theme = LocalPrimerTheme.current

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = theme.spacingTokens.large,
            vertical = theme.spacingTokens.medium,
        ),
        verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.small),
    ) {
        items(
            items = state.paymentMethods,
            key = { it.id },
        ) { paymentMethod ->
            AllVaultedPaymentMethodCard(
                paymentMethod = paymentMethod,
                state = VaultedMethodCardState(
                    isSelected = state.selectedPaymentMethodId == paymentMethod.id,
                    editMode = state.editMode,
                    isDeleting = state.isDeleting && state.deletingPaymentMethodId == paymentMethod.id,
                ),
                onClick = {
                    when (state.editMode) {
                        PrimerVaultedScope.State.EditMode.View -> {
                            // Hide the modal first, then select the payment method
                            coroutineScope.launch {
                                sheetState.hide()
                                viewModel.selectPaymentMethod(paymentMethod.id)
                            }
                        }
                        PrimerVaultedScope.State.EditMode.Edit -> {
                            // No selection in edit mode
                        }
                    }
                },
                onDeleteClick = {
                    viewModel.showDeleteConfirmation(paymentMethod.id)
                },
            )
        }
    }
}

@Composable
private fun EmptyStateContent() {
    val theme = LocalPrimerTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        ) {
            Text(
                text = stringResource(R.string.primer_components_vaulted_empty_state_title),
                style = theme.typographyTokens.titleLarge.toTextStyle(),
                color = theme.colorTokens().primerColorTextPrimary,
            )
            Text(
                text = stringResource(R.string.primer_components_vaulted_empty_state_description),
                style = theme.typographyTokens.bodyMedium.toTextStyle(),
                color = theme.colorTokens().primerColorTextSecondary,
            )
        }
    }
}
