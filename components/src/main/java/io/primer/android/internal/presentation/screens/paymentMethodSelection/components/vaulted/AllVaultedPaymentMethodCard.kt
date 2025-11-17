package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.vaulted

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.components.R
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.scope.PrimerVaultedScope

/**
 * Card component for displaying a vaulted payment method in the AllMethods stage.
 * Supports both View mode (with selection indicator) and Edit mode (with delete button).
 */
@Composable
internal fun AllVaultedPaymentMethodCard(
    paymentMethod: PrimerVaultedPaymentMethod,
    state: VaultedMethodCardState,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VaultedMethodManagerItem(
        state = state,
        onClick = onClick,
        onDeleteClick = onDeleteClick,
        modifier = modifier,
    ) {
        VaultedPaymentMethodContent(
            paymentMethod = paymentMethod,
            isSelected = state.isSelected,
            onClick = { }, // No-op, click handled by VaultedMethodManagerItem wrapper
            enableClickable = false, // Disable internal clickable, let wrapper handle it
        )
    }
}

@Composable
private fun VaultedMethodManagerItem(
    state: VaultedMethodCardState,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    detailsContent: @Composable () -> Unit,
) {
    val theme = LocalPrimerTheme.current

    val shape = RoundedCornerShape(theme.radiusTokens.medium)
    val borderColor = if (state.isSelected) {
        theme.colorTokens().primerColorBorderOutlinedSelected
    } else {
        theme.colorTokens().primerColorBorderOutlinedDefault
    }
    val backgroundColor = theme.colorTokens().primerColorBackground

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.spacingTokens.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Center: Payment method details
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(shape)
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape,
                )
                .clickable(
                    enabled = state.editMode == PrimerVaultedScope.State.EditMode.View && !state.isDeleting,
                    onClick = onClick,
                )
                .padding(theme.spacingTokens.medium),
        ) {
            detailsContent()
        }
        RightSideIndicator(state, theme, onDeleteClick)
    }
}

@Composable
private fun RightSideIndicator(state: VaultedMethodCardState, theme: PrimerTheme, onDeleteClick: () -> Unit) {
    AnimatedContent(
        targetState = state.editMode,
        transitionSpec = {
            (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith
                (fadeOut() + scaleOut(targetScale = 0.8f))
        },
        label = "editModeTransition",
    ) { editMode ->
        when (editMode) {
            PrimerVaultedScope.State.EditMode.Edit -> {
                if (state.isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(theme.sizeTokens.medium),
                        color = theme.colorTokens().primerColorBrand,
                        strokeWidth = 2.dp,
                    )
                } else {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(theme.sizeTokens.medium),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = stringResource(
                                id = R.string.primer_components_vaulted_delete_content_description,
                            ),
                        )
                    }
                }
            }

            PrimerVaultedScope.State.EditMode.View -> {
                // Empty space in view mode, but AnimatedContent handles the transition
                Box(modifier = Modifier.size(0.dp))
            }
        }
    }
}
