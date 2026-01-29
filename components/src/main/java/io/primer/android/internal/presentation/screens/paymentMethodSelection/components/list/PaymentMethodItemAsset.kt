package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.ui.assets.PrimerAsset
import io.primer.android.components.ui.assets.PrimerPaymentMethodAsset
import io.primer.android.components.ui.assets.PrimerPaymentMethodBackgroundColor
import io.primer.android.components.ui.assets.PrimerPaymentMethodColor

/**
 * Renders a payment method item using asset data from the backend.
 * Uses the slot-based [PaymentMethodItemLayout] for consistent positioning.
 */
@Composable
internal fun PaymentMethodItemAsset(
    asset: PrimerPaymentMethodAsset,
    onPaymentMethodSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val styling = rememberAssetStyling(asset, isDarkTheme)

    PaymentMethodItem(
        modifier = modifier.testTag("primer_payment_method_${asset.paymentMethodType.lowercase()}"),
        backgroundColor = styling.backgroundColor
            ?: LocalPrimerTheme.current.colorTokens().primerColorBackground,
        borderColor = styling.borderColor,
        borderRadius = LocalPrimerTheme.current.radiusTokens.medium,
        onPaymentMethodSelected = { onPaymentMethodSelected(asset.paymentMethodType) },
        accessibilityLabel = "Pay with ${asset.paymentMethodName}",
    ) {
        PaymentMethodItemLayout(
            iconPosition = asset.iconPosition,
            icon = styling.logo?.let { logo ->
                {
                    Image(
                        bitmap = logo,
                        contentDescription = null,
                        modifier = Modifier.heightIn(max = LocalPrimerTheme.current.sizeTokens.large),
                    )
                }
            },
            text = asset.text?.takeIf { it.isNotBlank() }?.let { text ->
                {
                    Text(
                        text = text,
                        style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                        color = styling.textColor
                            ?: LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                    )
                }
            },
        )
    }
}

/**
 * Holds computed styling values for a payment method asset.
 */
private data class AssetStyling(
    val backgroundColor: Color?,
    val borderColor: Color?,
    val textColor: Color?,
    val cornerRadius: Dp?,
    val logo: ImageBitmap?,
)

@Composable
private fun rememberAssetStyling(
    asset: PrimerPaymentMethodAsset,
    isDarkTheme: Boolean,
): AssetStyling {
    return remember(asset, isDarkTheme) {
        AssetStyling(
            backgroundColor = asset.paymentMethodBackgroundColor.resolveColor(isDarkTheme),
            borderColor = asset.borderColor?.resolveColor(isDarkTheme),
            textColor = asset.textColor?.resolveColor(isDarkTheme),
            cornerRadius = asset.cornerRadius?.let { Dp(it) },
            logo = asset.paymentMethodLogo.resolveLogo(isDarkTheme),
        )
    }
}

/** Resolves a theme-aware color from the color variants. */
private fun PrimerPaymentMethodBackgroundColor.resolveColor(isDarkTheme: Boolean): Color? {
    val colorInt = if (isDarkTheme) dark ?: colored else colored ?: light
    return colorInt?.let { Color(it) }
}

/** Resolves a theme-aware color from the color variants. */
private fun PrimerPaymentMethodColor.resolveColor(isDarkTheme: Boolean): Color? {
    val colorInt = if (isDarkTheme) dark ?: colored else colored ?: light
    return colorInt?.let { Color(it) }
}

/** Resolves a theme-aware logo from the logo variants. */
private fun PrimerAsset.resolveLogo(isDarkTheme: Boolean): ImageBitmap? {
    val drawable = if (isDarkTheme) dark ?: colored else colored ?: light
    return drawable?.toBitmap()?.asImageBitmap()
}
