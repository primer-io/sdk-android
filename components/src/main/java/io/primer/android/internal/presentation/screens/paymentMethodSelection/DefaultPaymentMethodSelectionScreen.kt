package io.primer.android.internal.presentation.screens.paymentMethodSelection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.CheckoutAppBar
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.surcharge.paymentMethodsList
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

@Composable
internal fun PrimerPaymentMethodSelectionScope.DefaultPaymentMethodSelectionScreen() {
    val state = state.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val formattedAmount = formatTitleAmount()
    val title = if (formattedAmount.isNotEmpty()) {
        context.getString(R.string.primer_components_payment_method_selection_pay_amount, formattedAmount)
    } else {
        context.getString(R.string.primer_components_payment_method_selection_pay)
    }

    Column {
        CheckoutAppBar(
            title = title,
            onCancelClick = { onCancel() },
        )
        LazyColumn(
            contentPadding = PaddingValues(
                start = LocalPrimerTheme.current.spacingTokens.large,
                end = LocalPrimerTheme.current.spacingTokens.large,
                bottom = LocalPrimerTheme.current.spacingTokens.large,
            ),
            verticalArrangement = Arrangement.spacedBy(LocalPrimerTheme.current.spacingTokens.small),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(vertical = LocalPrimerTheme.current.spacingTokens.small),
                    text = stringResource(R.string.primer_components_payment_method_selection_description),
                    style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                    color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                )
            }

            paymentMethodsList(state.paymentMethods, this@DefaultPaymentMethodSelectionScreen)
        }
    }
}
