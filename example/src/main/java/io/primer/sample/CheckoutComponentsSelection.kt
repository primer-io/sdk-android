package io.primer.sample

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.wallet.button.ButtonConstants
import io.primer.android.PrimerCheckout
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.GooglePayButtonOptions
import io.primer.android.data.settings.PrimerDebugOptions
import io.primer.android.data.settings.PrimerGooglePayOptions
import io.primer.android.data.settings.PrimerKlarnaOptions
import io.primer.android.data.settings.PrimerPaymentMethodOptions
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.scope.PrimerCheckoutScope
import io.primer.sample.demos.AnimatedSplashScreenDemo
import io.primer.sample.demos.BoldTypographyThemeDemo
import io.primer.sample.demos.ButtonedInputFieldsDemo
import io.primer.sample.demos.CheckoutDemo
import io.primer.sample.demos.CheckoutDemo.Companion.getBackground
import io.primer.sample.demos.CustomCardComponentDemo
import io.primer.sample.demos.CustomCardFormLayoutDemo
import io.primer.sample.demos.CustomKlarnaDemo
import io.primer.sample.demos.CustomSuccessDemo
import io.primer.sample.demos.DatePickerExpiryDemo
import io.primer.sample.demos.FullscreenDemo
import io.primer.sample.demos.GreenThemeDemo
import io.primer.sample.demos.HorizontalPaymentMethodsDemo
import io.primer.sample.demos.LargeSizesThemeDemo
import io.primer.sample.demos.LargeTypographyThemeDemo
import io.primer.sample.demos.MixedScopesDemo
import io.primer.sample.demos.NoRadiusThemeDemo
import io.primer.sample.demos.PrimerDemo
import io.primer.sample.demos.PurpleThemeDemo
import io.primer.sample.demos.RedThemeDemo
import io.primer.sample.demos.RegularTypographyThemeDemo
import io.primer.sample.demos.SingleInputFieldDemo
import io.primer.sample.demos.SmallSizesThemeDemo
import io.primer.sample.demos.SubmitOverrideDemo
import io.primer.sample.demos.ThreeTabsDemo


@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CheckoutComponentsSelection(clientToken: String?, onBackPress: () -> Unit) {
    var selectedDemo by remember { mutableStateOf<CheckoutDemo?>(null) }
    var checkoutScope by remember { mutableStateOf<PrimerCheckoutScope?>(null) }

    checkoutScope?.let { scope ->
        LaunchedEffect(scope) {
            scope.state.collect { state ->
                if (state is PrimerCheckoutScope.State.Dismissed) {
                    onBackPress()
                }
            }
        }
    }

    when (val current = selectedDemo) {
        null -> DemoSelectionGrid(
            onDemoSelected = { selectedDemo = it }
        )

        else -> {
            clientToken?.let {
                PrimerCheckout(
                    clientToken = it,
                    primerTheme = current.theme,
                    primerSettings = PrimerSettings(
                        debugOptions =
                            PrimerDebugOptions(false),
                        paymentMethodOptions = PrimerPaymentMethodOptions(
                            googlePayOptions = PrimerGooglePayOptions(
                                merchantName = "Darius",
                                buttonOptions = GooglePayButtonOptions(
                                    buttonType = ButtonConstants.ButtonType.DONATE,
                                    buttonTheme = ButtonConstants.ButtonTheme.LIGHT
                                ),
                            ),
                            klarnaOptions = PrimerKlarnaOptions(
                                recurringPaymentDescription = "This is custom description",
                                returnIntentUrl = Uri.Builder()
                                    .scheme("app")
                                    .authority("deeplink.return.activity")
                                    .build()
                                    .toString()
                            )
                        )
                    ),
                    scope = {
                        checkoutScope = this
                        current.render(this)
                    }
                )
            }
        }
    }
}

@Composable
private fun DemoSelectionGrid(
    onDemoSelected: (CheckoutDemo) -> Unit
) {
    val demos = remember { getAllDemos() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),

    ) {

        items(demos) { demo ->
            ListItem(
                modifier = Modifier.clickable { onDemoSelected(demo) },
                headlineContent = { Text(text = demo.title) },
                leadingContent = { Text(text = "${demo.customizationLevel}/5") },
                trailingContent = { CustomizationIndicator(demo = demo) },
                supportingContent = { Text(text = demo.description) }
            )

            HorizontalDivider()
        }
    }
}

@Composable
private fun CustomizationIndicator(demo: CheckoutDemo) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        (4 downTo 0).forEach { index ->
            Box(
                modifier = Modifier
                    .size(width = 32.dp, height = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < demo.customizationLevel) demo.getBackground()
                        else demo.getBackground().copy(alpha = 0.3f)
                    )
            )
        }
    }
}


private fun getAllDemos(): List<CheckoutDemo> = listOf(
    PrimerDemo,
    // Theme Demos
    RedThemeDemo,
    GreenThemeDemo,
    PurpleThemeDemo,
    NoRadiusThemeDemo,
    SmallSizesThemeDemo,
    LargeSizesThemeDemo,
    RegularTypographyThemeDemo,
    BoldTypographyThemeDemo,
    LargeTypographyThemeDemo,
    // Other Demos
    FullscreenDemo,
    AnimatedSplashScreenDemo,
    CustomSuccessDemo,
    CustomCardComponentDemo,
    CustomKlarnaDemo,
    SubmitOverrideDemo,
    HorizontalPaymentMethodsDemo,
    DatePickerExpiryDemo,
    MixedScopesDemo,
    CustomCardFormLayoutDemo,
    SingleInputFieldDemo,
    ButtonedInputFieldsDemo,
    ThreeTabsDemo,
)
