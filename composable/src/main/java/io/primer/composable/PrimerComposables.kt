package io.primer.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.composable.internal.presentation.screens.card.components.input.Input
import io.primer.composable.internal.presentation.screens.card.components.SubmitButton
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
    text: String,
) = SubmitButton(modifier, text)

@Composable
fun CardFormScope.PrimerInput(
    modifier: Modifier = Modifier,
    type: PrimerInputElementType,
) = Input(modifier, type)
