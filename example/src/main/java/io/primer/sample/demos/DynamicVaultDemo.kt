package io.primer.sample.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.PrimerTheme
import io.primer.android.api.checkout.PrimerCheckoutHost
import io.primer.android.api.checkout.rememberPrimerCheckoutController
import io.primer.android.api.components.card.CardFormDefaults
import io.primer.android.api.components.card.PrimerCardForm
import io.primer.android.api.components.card.PrimerCardFormController
import io.primer.android.api.components.card.rememberCardFormController
import io.primer.android.api.components.paymentMethods.PaymentMethodsDefaults
import io.primer.android.api.components.paymentMethods.PrimerPaymentMethodsController
import io.primer.android.api.components.paymentMethods.rememberPaymentMethodsController
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.api.state.PrimerCheckoutState
import io.primer.android.core.ExperimentalPrimerApi
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.tokens.LightColorTokens
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import java.text.NumberFormat
import java.util.Currency

// region Theme & Colors
private object DemoColors {
    val Background = Color(0xFFF5F0EB)
    val InfoRow = Color(0xFFDBCBC2)
    val CashbackYellow = Color(0xFFFFF8E1)
    val Orange = Color(0xFFFF8C00)
}

private val DemoTheme = PrimerTheme(
    lightColorTokens = object : LightColorTokens() {
        override val primerColorBackground = Color.White
        override val primerColorBorderOutlinedDefault = Color.Black
        override val primerColorBorderOutlinedFocus = DemoColors.Orange
    }
)
// endregion

private const val BOTTOM_SHEET_HEIGHT = 180

/**
 * Dynamic Vault Demo
 *
 * Demonstrates the ability to toggle vaultOnSuccess dynamically via a switch.
 */
@OptIn(ExperimentalPrimerApi::class)
@Composable
fun DynamicVaultDemo(
    clientToken: String,
    settings: PrimerSettings,
) {
    val checkout = rememberPrimerCheckoutController(clientToken, settings)
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    PrimerCheckoutHost(
        checkout = checkout,
        theme = DemoTheme,
    ) {
        when (checkoutState) {
            is PrimerCheckoutState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PrimerCheckoutState.Failure -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${(checkoutState as PrimerCheckoutState.Failure).error}")
                }
            }

            else -> {
                val paymentMethodState = rememberPaymentMethodsController(checkout)
                val cardFormState = rememberCardFormController(checkout)

                DemoCheckoutContent(
                    checkout = checkout,
                    paymentMethodState = paymentMethodState,
                    cardFormState = cardFormState,
                )
            }
        }
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun DemoCheckoutContent(
    checkout: PrimerCheckoutController,
    paymentMethodState: PrimerPaymentMethodsController,
    cardFormState: PrimerCardFormController,
) {
    var selectedPaymentMethod by remember { mutableStateOf<PrimerComposablePaymentMethod?>(null) }
    val paymentMethods by paymentMethodState.paymentMethods.collectAsStateWithLifecycle()

    // Auto-select card payment method if none selected
    LaunchedEffect(paymentMethods) {
        if (selectedPaymentMethod == null) {
            paymentMethods
                .find { it.paymentMethodType == PaymentMethodType.PAYMENT_CARD.name }
                ?.let { selectedPaymentMethod = it }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DemoColors.Background)
    ) {
        ScrollableContent(
            paymentMethodState = paymentMethodState,
            cardFormState = cardFormState,
            onPaymentMethodSelected = { selectedPaymentMethod = it },
        )
        BottomSheet(
            checkout = checkout,
            paymentMethodState = paymentMethodState,
            selectedPaymentMethod = selectedPaymentMethod,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun ScrollableContent(
    paymentMethodState: PrimerPaymentMethodsController,
    cardFormState: PrimerCardFormController,
    onPaymentMethodSelected: (PrimerComposablePaymentMethod) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = BOTTOM_SHEET_HEIGHT.dp + 16.dp),
    ) {
        DemoCard {
            // Header
            Text("🇷🇴 Romania", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            LabeledDivider()
            Text("Package", style = MaterialTheme.typography.labelMedium)

            // Package Info
            InfoRow(Icons.Default.LocationOn, "Coverage", "Romania")
            InfoRow(Icons.Default.Info, "Data", "1 GB")
            InfoRow(Icons.Default.DateRange, "Validity", "3 days")
            CashbackBanner()

            // Payment Sections
            LabeledDivider("Billing info")
            CountrySelector()
            LabeledDivider("Pay with")
            PaymentMethodsList(
                paymentMethodState = paymentMethodState,
                onPaymentMethodSelected = onPaymentMethodSelected,
            )
            LabeledDivider("Pay with card")
            CardFormSection(
                paymentMethodState = paymentMethodState,
                cardFormController = cardFormState,
                onPaymentMethodSelected = onPaymentMethodSelected,
            )
        }
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun PaymentMethodsList(
    paymentMethodState: PrimerPaymentMethodsController,
    onPaymentMethodSelected: (PrimerComposablePaymentMethod) -> Unit,
) {
    val paymentMethods by paymentMethodState.paymentMethods.collectAsStateWithLifecycle()

    val supportedMethods = remember(paymentMethods) {
        paymentMethods.filter {
            it.paymentMethodType in listOf(
                PaymentMethodType.PAYPAL.name,
                PaymentMethodType.GOOGLE_PAY.name,
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        supportedMethods.forEach { method ->
            WhiteCard(padding = 0.dp) {
                ListItem(
                    modifier = Modifier.clickable { onPaymentMethodSelected(method) },
                    headlineContent = {
                        Text(method.paymentMethodName ?: method.paymentMethodType, fontWeight = FontWeight.Bold)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun CardFormSection(
    paymentMethodState: PrimerPaymentMethodsController,
    cardFormController: PrimerCardFormController,
    onPaymentMethodSelected: (PrimerComposablePaymentMethod) -> Unit,
) {
    val paymentMethods by paymentMethodState.paymentMethods.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitPointerEvent(PointerEventPass.Initial)
                    paymentMethods
                        .find { it.paymentMethodType == PaymentMethodType.PAYMENT_CARD.name }
                        ?.let { onPaymentMethodSelected(it) }
                }
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimerCardForm(
                controller = cardFormController,
                submitButton = { }, // We render the submit button in the bottom sheet
            )
            SaveCardToggle(cardFormState = cardFormController)
        }
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun SaveCardToggle(cardFormState: PrimerCardFormController) {
    val state by cardFormState.state.collectAsStateWithLifecycle()

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Switch(
            checked = state.vaultOnSuccess,
            onCheckedChange = { cardFormState.setVaultOnSuccess(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DemoColors.Orange
            ),
            enabled = !state.isLoading
        )
        Text("Save card", modifier = Modifier.padding(start = 8.dp))
    }
}

@OptIn(ExperimentalPrimerApi::class)
@Composable
private fun BottomSheet(
    checkout: PrimerCheckoutController,
    paymentMethodState: PrimerPaymentMethodsController,
    selectedPaymentMethod: PrimerComposablePaymentMethod?,
    modifier: Modifier = Modifier,
) {
    val checkoutState by checkout.state.collectAsStateWithLifecycle()
    val clientSession = (checkoutState as? PrimerCheckoutState.Ready)?.clientSession

    val formattedAmount = remember(clientSession?.totalAmount, clientSession?.currencyCode) {
        clientSession?.let { session ->
            session.currencyCode?.let { currencyCode ->
                NumberFormat.getCurrencyInstance().apply {
                    currency = Currency.getInstance(currencyCode)
                }.format((session.totalAmount ?: 0) / 100.0)
            }
        } ?: ""
    }

    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "By completing your order, you agree to our terms and conditions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total")
            Text(formattedAmount, fontWeight = FontWeight.Bold)
        }

        selectedPaymentMethod?.let { method ->
            if (method.paymentMethodType == PaymentMethodType.PAYMENT_CARD.name) {
                val state = rememberCardFormController(checkout)
                CardFormDefaults.SubmitButton(cardFormState = state)
            } else {
                PaymentMethodsDefaults.Method(
                    method = method,
                    onClick = { paymentMethodState.select(method) }
                )
            }
        }
    }
}

// region UI Components
@Composable
private fun DemoCard(content: @Composable ColumnScope.() -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = DemoColors.Background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun WhiteCard(padding: Dp = 16.dp, content: @Composable ColumnScope.() -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DemoColors.InfoRow, RoundedCornerShape(12.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = Color.Black)
        Text(
            label, Modifier
                .padding(start = 8.dp)
                .weight(1f), color = Color.Black, fontWeight = FontWeight.Light
        )
        Text(value, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
private fun CashbackBanner() {
    ListItem(
        modifier = Modifier
            .background(DemoColors.CashbackYellow, RoundedCornerShape(12.dp))
            .border(1.dp, DemoColors.Orange, RoundedCornerShape(12.dp)),
        leadingContent = { Icon(Icons.Default.Star, null, tint = DemoColors.Orange) },
        headlineContent = { Text("You'll earn in cashback from this purchase:", fontSize = 12.sp) },
        supportingContent = { Text("0.20 €", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun CountrySelector() {
    WhiteCard(padding = 0.dp) {
        ListItem(
            modifier = Modifier.clickable { },
            headlineContent = { Text("Romania", fontWeight = FontWeight.Medium) },
            trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun LabeledDivider(label: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(Modifier.weight(1f))
        if (label != null) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            HorizontalDivider(Modifier.weight(1f))
        }
    }
}
// endregion
