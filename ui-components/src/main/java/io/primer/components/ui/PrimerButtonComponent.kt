package io.primer.components.ui

import androidx.compose.foundation.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager

/**
 * A composable button for selecting a payment method in the checkout process. It displays the payment method's logo and
 * applies its associated background color.
 *
 * @param modifier The [Modifier] to be applied to the button.
 * @param paymentMethod The [PrimerHeadlessUniversalCheckoutPaymentMethod] associated with this button.
 * @param onMethodSelected Callback triggered when the button is clicked, passing the selected payment method.
 */
@Composable
fun PrimerButtonComponent(
    modifier: Modifier = Modifier,
    paymentMethod: PrimerHeadlessUniversalCheckoutPaymentMethod,
    onMethodSelected: (
        PrimerHeadlessUniversalCheckoutPaymentMethod,
    ) -> Unit, // TODO TWS: can be changed so that it doesn't take in object
) {
    // TODO TWS: switch to payment method resources
    val asset = PrimerHeadlessUniversalCheckoutAssetsManager.getPaymentMethodAsset(
        context = LocalContext.current,
        paymentMethodType = paymentMethod.paymentMethodType,
    )

    // TODO TWS: should probably not use Material widgets
    OutlinedButton(
        onClick = { onMethodSelected(paymentMethod) },
        colors = ButtonDefaults.buttonColors()
            .copy(
                containerColor = asset.paymentMethodBackgroundColor.colored?.let {
                    Color(it)
                } ?: Color.White,
            ), // TODO TWS!: should we allow overriding the color?
        modifier = modifier,
    ) {
        Image(
            bitmap = asset.paymentMethodLogo.colored?.toBitmap()?.asImageBitmap()!!, // TODO: make more robust; // TODO TWS!: should we allow overriding the bitmap?
            contentDescription = "Pay using ${paymentMethod.paymentMethodName}", // TODO TWS: localize caption
        )
    }
}
