package io.primer.android.internal.presentation.screens.paymentMethodSelection.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.components.R
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultPaymentMethodsSectionHeader() {
    SectionHeader(title = stringResource(R.string.primer_payment_selection_header))
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultPaymentMethodsSectionHeaderPreview() = PreviewContainer {
    DefaultPaymentMethodsSectionHeader()
}
