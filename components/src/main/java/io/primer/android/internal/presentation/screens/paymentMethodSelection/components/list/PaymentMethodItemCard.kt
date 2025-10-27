package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.primer.android.LocalPrimerTheme
import io.primer.android.PrimerTheme
import io.primer.android.components.PrimerPaymentMethodSelectionComponents
import io.primer.android.components.R
import io.primer.android.core.di.SdkContainer
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.scope.PrimerPaymentMethodSelectionScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun PrimerPaymentMethodSelectionScope.PaymentMethodItemCard(
    @Suppress("UnusedParameter") modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    PaymentMethodItem(
        borderColor = LocalPrimerTheme.current.colorTokens().primerColorBorderOutlinedDefault,
        onPaymentMethodSelected = { onPaymentMethodSelected(PaymentMethodType.PAYMENT_CARD.name) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (layoutDirection == LayoutDirection.Rtl) {
                Arrangement.End
            } else {
                Arrangement.Start
            },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_primer_credit_card),
                contentDescription = null,
                tint = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                modifier = Modifier.size(LocalPrimerTheme.current.sizeTokens.medium),
            )
            Text(
                text = stringResource(R.string.primer_components_select_payment_method_card),
                style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                modifier = Modifier.padding(
                    start = if (layoutDirection == LayoutDirection.Ltr) {
                        LocalPrimerTheme.current.spacingTokens.small
                    } else {
                        0.dp
                    },
                    end = if (layoutDirection == LayoutDirection.Rtl) {
                        LocalPrimerTheme.current.spacingTokens.small
                    } else {
                        0.dp
                    },
                ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Payment Method Item – Card")
@Composable
private fun PaymentMethodItemCardPreview() {
    CompositionLocalProvider(LocalPrimerTheme provides PrimerTheme()) {
        val scope = remember { PreviewPaymentMethodSelectionScope() }
        scope.PaymentMethodItemCard(
            modifier = Modifier.padding(LocalPrimerTheme.current.spacingTokens.small),
        )
    }
}

private class PreviewPaymentMethodSelectionScope : PrimerPaymentMethodSelectionScope {
    private val previewContainer = SdkContainer()
    private val previewState = MutableStateFlow(PrimerPaymentMethodSelectionScope.State())
    private val previewComponents = PrimerPaymentMethodSelectionComponents()

    override val components: PrimerPaymentMethodSelectionComponents
        get() = previewComponents

    override val state: StateFlow<PrimerPaymentMethodSelectionScope.State>
        get() = previewState

    override fun onPaymentMethodSelected(paymentMethod: String) = Unit

    override fun onCancel() = Unit

    override fun formatTitleAmount(): String = ""

    override fun formatSurchargeAmount(amountInCents: Int): String = ""

    override fun getSdkContainer(): SdkContainer = previewContainer
}
