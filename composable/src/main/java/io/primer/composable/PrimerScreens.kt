package io.primer.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.card.CardFormScreen
import io.primer.composable.internal.presentation.screens.error.ErrorScreen
import io.primer.composable.internal.presentation.screens.loading.LoadingScreen
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodSelectionScreen
import io.primer.composable.internal.presentation.screens.success.SuccessScreen
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
fun PrimerLoadingScreen(
    modifier: Modifier = Modifier
) = LoadingScreen(modifier)

@Composable
fun PaymentMethodSelectionScope.PrimerPaymentMethodSelectionScreen(
    modifier: Modifier = Modifier
) = PaymentMethodSelectionScreen(modifier)

@Composable
fun CardFormScope.PrimerCardFormScreen(
    modifier: Modifier = Modifier
) = CardFormScreen(modifier)

@Composable
fun PrimerErrorScreen(
    modifier: Modifier = Modifier,
    message: String
) = ErrorScreen(modifier, message)

@Composable
fun PrimerSuccessScreen(
    modifier: Modifier = Modifier,
    message: String
) = SuccessScreen(modifier, message)
