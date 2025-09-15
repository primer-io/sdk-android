package io.primer.android.internal.presentation.checkout.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import io.primer.android.LocalPrimerTheme
import io.primer.android.components.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onCancelClick: (() -> Unit)? = null,
) {
    val layoutDirection = LocalLayoutDirection.current
    TopAppBar(
        colors = TopAppBarColors(
            containerColor = LocalPrimerTheme.current.colorTokens().primerColorBackground,
            scrolledContainerColor = LocalPrimerTheme.current.colorTokens().primerColorBackground,
            navigationIconContentColor = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            titleContentColor = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            actionIconContentColor = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
        ),
        title = {
            Text(
                text = title,
                style = LocalPrimerTheme.current.typographyTokens.titleXlarge.toTextStyle(),
                color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
            )
        },
        navigationIcon = {
            onBackClick?.let {
                IconButton(onClick = it) {
                    val backIconRes = if (layoutDirection == LayoutDirection.Rtl) {
                        R.drawable.ic_primer_chevron_right
                    } else {
                        R.drawable.ic_primer_chevron_left
                    }
                    Icon(
                        painter = painterResource(id = backIconRes),
                        contentDescription = stringResource(R.string.primer_components_content_description_back),
                        tint = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                    )
                }
            }
        },
        actions = {
            onCancelClick?.let {
                TextButton(onClick = it) {
                    Text(
                        text = stringResource(R.string.primer_components_checkout_cancel),
                        style = LocalPrimerTheme.current.typographyTokens.titleLarge.toTextStyle(),
                        color = LocalPrimerTheme.current.colorTokens().primerColorTextPrimary,
                    )
                }
            }
        },
        modifier = modifier,
    )
}
