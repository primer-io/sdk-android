package io.primer.android.internal.presentation.screens.vault.selected

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R
import io.primer.android.internal.presentation.checkout.components.Screen
import io.primer.android.internal.presentation.checkout.navigation.LocalSheetNavController
import io.primer.android.internal.presentation.components.PrimerButton
import io.primer.android.internal.presentation.preview.PreviewContainer

@Composable
internal fun DefaultOtherPaymentMethodsButton() {
    val navController = LocalSheetNavController.current
    val theme = LocalPrimerTheme.current

    PrimerButton(
        onClick = {
            navController.popBackStack(route = Screen.Content, inclusive = false)
        },
        modifier = Modifier.fillMaxWidth(),
        borderColor = theme.colorTokens().primerColorBorderOutlinedDefault,
        backgroundColor = theme.colorTokens().primerColorBackground,
    ) {
        Text(
            stringResource(R.string.primer_vault_selected_button_other),
            style = theme.typographyTokens.titleLarge.toTextStyle(),
            color = theme.colorTokens().primerColorTextPrimary,
        )
    }
}

@Preview(name = "Default", showBackground = true)
@Composable
private fun DefaultOtherPaymentMethodsButtonPreview() = PreviewContainer {
    DefaultOtherPaymentMethodsButton()
}
