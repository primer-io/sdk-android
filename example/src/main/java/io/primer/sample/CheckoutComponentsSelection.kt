package io.primer.sample

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.sample.demos.BasicSheetCheckoutDemo
import io.primer.sample.demos.CustomCardFormDemo
import io.primer.sample.demos.CustomCardFormSheetDemo
import io.primer.sample.demos.CustomGridPaymentMethodsDemo
import io.primer.sample.demos.CustomPaymentMethodListDemo
import io.primer.sample.demos.InlineCardFormDemo
import io.primer.sample.demos.InlineCheckoutDemo
import io.primer.sample.demos.CustomThemeDemo
import io.primer.sample.demos.MerchantNavigationDemo
import io.primer.sample.demos.PaymentMethodListOnlyDemo
import io.primer.sample.demos.RadioSelectionDemo
import io.primer.sample.demos.VaultManagementDemo
import io.primer.sample.demos.VaultModeInlineDemo
import io.primer.sample.demos.VaultedPaymentMethodsDemo
import io.primer.sample.demos.RedThemeDemoV2
import io.primer.sample.demos.GreenThemeDemoV2
import io.primer.sample.demos.PurpleThemeDemoV2
import io.primer.sample.demos.NoRadiusThemeDemoV2
import io.primer.sample.demos.SmallSizesThemeDemoV2
import io.primer.sample.demos.LargeSizesThemeDemoV2
import io.primer.sample.demos.LightTypographyThemeDemoV2
import io.primer.sample.demos.BoldTypographyThemeDemoV2
import io.primer.sample.demos.LargeTypographyThemeDemoV2
import io.primer.sample.demos.CustomFontThemeDemoV2
import io.primer.sample.demos.CustomResultScreensDemo
import io.primer.sample.demos.DynamicVaultDemo
import io.primer.sample.demos.RefreshClientSessionDemo

/**
 * Selection screen for CC API demos.
 *
 * The API uses:
 * - rememberPrimerCheckout() as the entry point
 * - PrimerCheckoutSheet for modal presentation
 * - PrimerCheckoutInline for embedded presentation
 * - Action pattern for explicit flow control
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun CheckoutComponentsSelection(
    clientToken: String,
    settings: PrimerSettings,
    onBackPress: () -> Unit,
) {
    var selectedDemo by remember { mutableStateOf<Demo?>(null) }

    // Show selected demo
    selectedDemo?.let { demo ->
        when (demo) {
            Demo.BasicSheet -> BasicSheetCheckoutDemo(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.CustomCardForm -> CustomCardFormSheetDemo(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.CustomPaymentMethodList -> CustomPaymentMethodListDemo(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.InlineCheckout -> InlineCheckoutDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.VaultManagement -> VaultManagementDemo(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.VaultedPaymentMethods -> VaultedPaymentMethodsDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.DynamicVaultDemo -> DynamicVaultDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.VaultModeInline -> VaultModeInlineDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.InlineMerchant -> MerchantNavigationDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.InlineRadio -> RadioSelectionDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.InlineCardForm -> InlineCardFormDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.CustomTheme -> CustomThemeDemo(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.PaymentMethodListOnly -> PaymentMethodListOnlyDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.CustomGridPaymentMethods -> CustomGridPaymentMethodsDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.FullyCustomCard -> CustomCardFormDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.RedTheme -> RedThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.GreenTheme -> GreenThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.PurpleTheme -> PurpleThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.NoRadiusTheme -> NoRadiusThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.SmallSizesTheme -> SmallSizesThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.LargeSizesTheme -> LargeSizesThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.LightTypographyTheme -> LightTypographyThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.BoldTypographyTheme -> BoldTypographyThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.LargeTypographyTheme -> LargeTypographyThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.CustomFontTheme -> CustomFontThemeDemoV2(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )

            Demo.RefreshClientSession -> RefreshClientSessionDemo(
                clientToken = clientToken,
                settings = settings,
            )

            Demo.CustomResultScreens -> CustomResultScreensDemo(
                clientToken = clientToken,
                settings = settings,
                onDismiss = { selectedDemo = null },
            )
        }
        return
    }

    // Show demo selection grid
    DemoSelectionList(
        onDemoSelected = { selectedDemo = it },
    )
}

@Composable
private fun DemoSelectionList(
    onDemoSelected: (Demo) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "V2 API Demos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These demos showcase the new Compose API with action-based flow control.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(Demo.entries) { demo ->
            DemoCard(
                demo = demo,
                onClick = { onDemoSelected(demo) },
            )
        }
    }
}

@Composable
private fun DemoCard(
    demo: Demo,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = demo.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = demo.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )
        }
    }
}

@Composable
private fun ResultDisplay(
    result: PrimerCheckoutState,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Payment Result",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (result) {
                    is PrimerCheckoutState.Success -> Color(0xFFE8F5E9)
                    is PrimerCheckoutState.Failure -> Color(0xFFFFEBEE)
                    is PrimerCheckoutState.Cancelled -> Color(0xFFFFF8E1)
                    is PrimerCheckoutState.TokenCreated -> Color(0xFFE3F2FD)
                    else -> Color.White
                },
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = when (result) {
                        is PrimerCheckoutState.Success -> "Success"
                        is PrimerCheckoutState.Failure -> "Failed"
                        is PrimerCheckoutState.Cancelled -> "Cancelled"
                        is PrimerCheckoutState.TokenCreated -> "Token Created (MANUAL flow)"
                        else -> "Unknown"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (result) {
                    is PrimerCheckoutState.Success -> {
                        Text("Payment ID: ${result.checkoutData.payment.id}")
                        result.checkoutData.payment.orderId.let { orderId ->
                            Text("Order ID: $orderId", color = Color.Gray)
                        }
                    }

                    is PrimerCheckoutState.Failure -> {
                        Text("Error: ${result.error}")
                    }

                    is PrimerCheckoutState.Cancelled -> {
                        Text("Payment was cancelled by the user.")
                    }

                    is PrimerCheckoutState.TokenCreated -> {
                        Text("Token: ${result.token}")
                        Text("Method: ${result.paymentMethodType}", color = Color.Gray)
                    }

                    else -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.Button(
            onClick = onDismiss,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Back to Demos")
        }
    }
}

private enum class Demo(
    val title: String,
    val description: String,
) {
    BasicSheet(
        title = "Basic Sheet Checkout",
        description = "Simplest integration with all default UI in a modal bottom sheet.",
    ),
    CustomCardForm(
        title = "Inline Card Form",
        description = "CardForm embedded inline with same UI as V1's CardFormDefaults.",
    ),
    CustomPaymentMethodList(
        title = "Custom Payment Method List",
        description = "Sheet with custom styled payment method items.",
    ),
    InlineCheckout(
        title = "Inline Checkout (Embedded)",
        description = "Card form embedded in layout - developer controls visibility.",
    ),
    VaultManagement(
        title = "Vault Management",
        description = "Display and manage saved payment methods with pay/delete actions.",
    ),
    VaultedPaymentMethods(
        title = "Vaulted Payment Methods (State)",
        description = "Custom UI by observing state.methods - swipe to delete, custom card design.",
    ),
    DynamicVaultDemo(
        title = "Dynamic Vault Demo",
        description = "Custom UI to toggle vault on success",
    ),
    VaultModeInline(
        title = "Vault Mode Inline",
        description = "Custom navigation: select card → enter details → get token displayed.",
    ),
    InlineMerchant(
        title = "Inline Merchant",
        description = "Custom navigation: select card → enter details → get token displayed.",
    ),
    InlineRadio(
        title = "Inline Radio",
        description = "Custom navigation: select card → enter details → get token displayed.",
    ),
    InlineCardForm(
        title = "Inline Card Form + Discount",
        description = "Custom card form layout with discount code field.",
    ),
    CustomTheme(
        title = "Custom Theme",
        description = "Purple brand theme with rounded corners and custom typography.",
    ),
    PaymentMethodListOnly(
        title = "Payment Methods Only (FlowSheet)",
        description = "Inline list where onClick opens card form in FlowSheet automatically.",
    ),
    CustomGridPaymentMethods(
        title = "Custom Grid (rememberActions)",
        description = "2-column grid using rememberPaymentMethodActions() for custom UI.",
    ),
    FullyCustomCard(
        title = "Custom Card",
        description = "2-column grid using rememberPaymentMethodActions() for custom UI.",
    ),

    // Theme Demos
    RedTheme(
        title = "Red Brand Theme",
        description = "Red brand color with custom accents.",
    ),
    GreenTheme(
        title = "Green Nature Theme",
        description = "Green brand with nature-inspired colors.",
    ),
    PurpleTheme(
        title = "Purple Creative Theme",
        description = "Purple brand with creative vibes.",
    ),
    NoRadiusTheme(
        title = "No Radius Theme",
        description = "Sharp, rectangular design with zero border radius.",
    ),
    SmallSizesTheme(
        title = "Small Sizes Theme",
        description = "Compact design with smaller component sizes.",
    ),
    LargeSizesTheme(
        title = "Large Sizes Theme",
        description = "Bold design with larger component sizes.",
    ),
    LightTypographyTheme(
        title = "Light Typography (300)",
        description = "Clean, minimal typography with light font weights.",
    ),
    BoldTypographyTheme(
        title = "Bold Typography (700)",
        description = "Strong, impactful typography with bold font weights.",
    ),
    LargeTypographyTheme(
        title = "Large Typography (32sp)",
        description = "Accessibility-focused design with large text sizes.",
    ),
    CustomFontTheme(
        title = "Custom Font",
        description = "Custom font resource usage in the checkout flow.",
    ),

    // Utility Demos
    RefreshClientSession(
        title = "Refresh Client Session",
        description = "Test that refreshClientSession() updates all observers (state, payment methods, vault).",
    ),

    // Custom Screens Demo
    CustomResultScreens(
        title = "Custom Result Screens",
        description = "Custom loading, success, and error screens with logging and simulated API calls.",
    ),
}
