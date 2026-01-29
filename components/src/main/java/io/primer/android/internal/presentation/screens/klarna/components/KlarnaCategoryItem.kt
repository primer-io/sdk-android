package io.primer.android.internal.presentation.screens.klarna.components

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.constants.PaymentMethodColors
import io.primer.android.scope.PrimerKlarnaScope

/** Payment category card with selection state and embedded payment view. */
@Composable
internal fun KlarnaCategoryItem(
    category: PrimerKlarnaScope.Category,
    isSelected: Boolean,
    isLoading: Boolean,
    paymentView: View?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = LocalPrimerTheme.current
    val colorTokens = theme.colorTokens()
    val shape = RoundedCornerShape(theme.radiusTokens.medium)
    val backgroundColor = if (isSelected) colorTokens.primerColorGray100 else colorTokens.primerColorBackground
    val borderColor = if (isSelected) colorTokens.primerColorBrand else colorTokens.primerColorBorderOutlinedDefault

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor, shape)
            .border(theme.borderWidthTokens.thin, borderColor, shape)
            .clickable(onClick = onClick),
    ) {
        CategoryHeader(category, isSelected, isLoading)
        CategoryPaymentView(isSelected, paymentView)
    }
}

@Composable
private fun CategoryHeader(category: PrimerKlarnaScope.Category, isSelected: Boolean, isLoading: Boolean) {
    val theme = LocalPrimerTheme.current
    val spacing = theme.spacingTokens
    val colorTokens = theme.colorTokens()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.medium, vertical = spacing.xlarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        KlarnaCategoryIcon()
        Text(
            text = category.name,
            style = theme.typographyTokens.bodyLarge.toTextStyle(),
            color = colorTokens.primerColorTextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (isSelected && !isLoading) {
            Icon(painterResource(R.drawable.ic_check_blue), null, tint = Color.Unspecified)
        }
        if (isLoading) {
            CircularProgressIndicator(
                Modifier.size(theme.sizeTokens.medium),
                colorTokens.primerColorBrand,
                strokeWidth = theme.borderWidthTokens.medium,
            )
        }
    }
}

@Composable
private fun KlarnaCategoryIcon() {
    val theme = LocalPrimerTheme.current
    Box(
        modifier = Modifier
            .background(PaymentMethodColors.klarnaPink, RoundedCornerShape(theme.radiusTokens.medium))
            .padding(theme.spacingTokens.small),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(R.drawable.ic_primer_klarna_logo), null, tint = Color.Unspecified)
    }
}

@Composable
private fun CategoryPaymentView(isSelected: Boolean, paymentView: View?) {
    val spacing = LocalPrimerTheme.current.spacingTokens
    AnimatedVisibility(
        visible = isSelected && paymentView != null,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        paymentView?.let { view ->
            KlarnaPaymentView(
                view,
                Modifier.fillMaxWidth().padding(spacing.small, spacing.small, spacing.small, spacing.medium),
            )
        }
    }
}
