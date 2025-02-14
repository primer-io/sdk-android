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

@Composable
fun PrimerButtonComponent(
    modifier: Modifier = Modifier,
    paymentMethod: PrimerHeadlessUniversalCheckoutPaymentMethod,
    onMethodSelected: (PrimerHeadlessUniversalCheckoutPaymentMethod) -> Unit,
) {
    val asset = PrimerHeadlessUniversalCheckoutAssetsManager.getPaymentMethodAsset(
        context = LocalContext.current,
        paymentMethodType = paymentMethod.paymentMethodType,
    )
    OutlinedButton(
        onClick = {
            onMethodSelected(paymentMethod)
        },
        colors = ButtonDefaults.buttonColors()
            .copy(containerColor = asset.paymentMethodBackgroundColor.colored?.let { Color(it) } ?: Color.White),
        modifier = modifier,
    ) {
        Image(
            bitmap = asset.paymentMethodLogo.colored?.toBitmap()?.asImageBitmap()!!,
            contentDescription = "Pay using ${paymentMethod.paymentMethodName}",
        )
    }
}
