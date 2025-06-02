package io.primer.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.composable.internal.presentation.screens.card.SubmitButton
import io.primer.composable.internal.presentation.screens.paymentMethodSelection.PaymentMethodItem
import io.primer.composable.model.PrimerPaymentMethod
import io.primer.composable.scope.CardFormScope
import io.primer.composable.scope.PaymentMethodSelectionScope

@Composable
fun PaymentMethodSelectionScope.PrimerPaymentMethodItem(
    modifier: Modifier = Modifier,
    primerPaymentMethod: PrimerPaymentMethod,
) = PaymentMethodItem(modifier, primerPaymentMethod)

@Composable
fun CardFormScope.PrimerSubmitButton(
    modifier: Modifier = Modifier,
    text: String
) = SubmitButton(modifier, text)
